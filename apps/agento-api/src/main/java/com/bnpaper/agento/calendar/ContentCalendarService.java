package com.bnpaper.agento.calendar;

import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.product.ProductFact;
import com.bnpaper.agento.product.ProductFactRepository;
import com.bnpaper.agento.workflow.WorkerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContentCalendarService {

    private final ContentCalendarRepository calendarRepo;
    private final CalendarItemRepository itemRepo;
    private final BrandProfileRepository brandRepo;
    private final ProductFactRepository productRepo;
    private final WorkerClient workerClient;

    public List<ContentCalendarDto.CalendarResponse> listCalendars() {
        return calendarRepo.findAllByOrderByCreatedAtDesc().stream()
                .map(c -> ContentCalendarDto.toCalendarResponse(c, itemRepo.countByCalendarId(c.getId())))
                .toList();
    }

    public ContentCalendarDto.CalendarResponse findCalendar(UUID id) {
        ContentCalendar cal = getOrThrow(id);
        return ContentCalendarDto.toCalendarResponse(cal, itemRepo.countByCalendarId(id));
    }

    @Transactional
    public ContentCalendarDto.CalendarResponse createCalendar(ContentCalendarDto.CreateRequest req) {
        if (req.getPeriodEnd().isBefore(req.getPeriodStart())) {
            throw new IllegalArgumentException("Period end must be on or after period start");
        }
        ContentCalendar cal = ContentCalendar.builder()
                .name(req.getName())
                .periodStart(req.getPeriodStart())
                .periodEnd(req.getPeriodEnd())
                .objective(req.getObjective())
                .status(CalendarStatus.DRAFT)
                .build();
        cal = calendarRepo.save(cal);
        return ContentCalendarDto.toCalendarResponse(cal, 0);
    }

    @Transactional
    public ContentCalendarDto.CalendarResponse updateCalendar(UUID id, ContentCalendarDto.UpdateRequest req) {
        ContentCalendar cal = getOrThrow(id);
        if (req.getName() != null) cal.setName(req.getName());
        if (req.getPeriodStart() != null) cal.setPeriodStart(req.getPeriodStart());
        if (req.getPeriodEnd() != null) cal.setPeriodEnd(req.getPeriodEnd());
        if (req.getObjective() != null) cal.setObjective(req.getObjective());
        if (req.getStatus() != null) {
            try {
                cal.setStatus(CalendarStatus.valueOf(req.getStatus()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown calendar status: " + req.getStatus());
            }
        }
        cal = calendarRepo.save(cal);
        int count = itemRepo.countByCalendarId(id);
        return ContentCalendarDto.toCalendarResponse(cal, count);
    }

    @Transactional
    public void deleteCalendar(UUID id) {
        if (!calendarRepo.existsById(id)) {
            throw new ResourceNotFoundException("ContentCalendar", id);
        }
        calendarRepo.deleteById(id);
    }

    // ── Calendar Items ─────────────────────────────────────────────────────────

    public List<ContentCalendarDto.ItemResponse> listItems(UUID calendarId) {
        assertCalendarExists(calendarId);
        return itemRepo.findByCalendarIdOrderByPlannedDateAsc(calendarId).stream()
                .map(ContentCalendarDto::toItemResponse)
                .toList();
    }

    @Transactional
    public ContentCalendarDto.ItemResponse addItem(UUID calendarId, ContentCalendarDto.ItemCreateRequest req) {
        assertCalendarExists(calendarId);
        CalendarItem item = CalendarItem.builder()
                .calendarId(calendarId)
                .plannedDate(req.getPlannedDate())
                .channel(req.getChannel())
                .contentType(req.getContentType())
                .contentAngle(req.getContentAngle())
                .targetAudience(req.getTargetAudience())
                .hookDirection(req.getHookDirection())
                .ctaDirection(req.getCtaDirection())
                .status(CalendarItemStatus.PLANNED)
                .build();
        item = itemRepo.save(item);
        return ContentCalendarDto.toItemResponse(item);
    }

    @Transactional
    public ContentCalendarDto.ItemResponse updateItem(UUID calendarId, UUID itemId,
                                                       ContentCalendarDto.ItemUpdateRequest req) {
        assertCalendarExists(calendarId);
        CalendarItem item = itemRepo.findById(itemId)
                .filter(i -> i.getCalendarId().equals(calendarId))
                .orElseThrow(() -> new ResourceNotFoundException("CalendarItem", itemId));

        if (req.getPlannedDate() != null) item.setPlannedDate(req.getPlannedDate());
        if (req.getChannel() != null) item.setChannel(req.getChannel());
        if (req.getContentType() != null) item.setContentType(req.getContentType());
        if (req.getContentAngle() != null) item.setContentAngle(req.getContentAngle());
        if (req.getTargetAudience() != null) item.setTargetAudience(req.getTargetAudience());
        if (req.getHookDirection() != null) item.setHookDirection(req.getHookDirection());
        if (req.getCtaDirection() != null) item.setCtaDirection(req.getCtaDirection());

        item = itemRepo.save(item);
        return ContentCalendarDto.toItemResponse(item);
    }

    @Transactional
    public void deleteItem(UUID calendarId, UUID itemId) {
        assertCalendarExists(calendarId);
        CalendarItem item = itemRepo.findById(itemId)
                .filter(i -> i.getCalendarId().equals(calendarId))
                .orElseThrow(() -> new ResourceNotFoundException("CalendarItem", itemId));
        itemRepo.delete(item);
    }

    // ── AI Planning ───────────────────────────────────────────────────────────

    @Transactional
    public List<ContentCalendarDto.ItemResponse> planCalendar(UUID calendarId) {
        ContentCalendar cal = getOrThrow(calendarId);

        BrandProfile brand = brandRepo.findFirstByOrderByCreatedAtAsc()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No brand profile found. Please create a brand profile first."));
        List<ProductFact> products = productRepo.findAll();

        long dayCount = ChronoUnit.DAYS.between(cal.getPeriodStart(), cal.getPeriodEnd()) + 1;
        int numItems = (int) Math.min(dayCount * 2, 30);

        ContentCalendarDto.PlanWorkerRequest workerReq = ContentCalendarDto.PlanWorkerRequest.builder()
                .calendarId(calendarId.toString())
                .brand(brandToMap(brand))
                .products(productsToList(products))
                .periodStart(cal.getPeriodStart().toString())
                .periodEnd(cal.getPeriodEnd().toString())
                .objective(cal.getObjective() != null ? cal.getObjective() : "")
                .numItems(numItems)
                .build();

        ContentCalendarDto.PlanWorkerResponse workerResp = workerClient.planCalendar(workerReq);

        if (workerResp == null || workerResp.getSuggestions() == null
                || workerResp.getSuggestions().isEmpty()) {
            throw new IllegalStateException("Calendar planner returned no suggestions");
        }

        // Replace existing PLANNED items (keep COMPLETED/FAILED/GENERATING ones)
        List<CalendarItem> existing = itemRepo.findByCalendarIdOrderByPlannedDateAsc(calendarId);
        List<UUID> toDelete = existing.stream()
                .filter(i -> i.getStatus() == CalendarItemStatus.PLANNED)
                .map(CalendarItem::getId)
                .toList();
        itemRepo.deleteAllById(toDelete);

        List<CalendarItem> newItems = new ArrayList<>();
        for (ContentCalendarDto.ItemSuggestion s : workerResp.getSuggestions()) {
            LocalDate date;
            try {
                date = LocalDate.parse(s.getPlannedDate());
            } catch (Exception e) {
                log.warn("Could not parse planned date '{}', skipping suggestion", s.getPlannedDate());
                continue;
            }
            CalendarItem item = CalendarItem.builder()
                    .calendarId(calendarId)
                    .plannedDate(date)
                    .channel(s.getChannel())
                    .contentType(s.getContentType())
                    .contentAngle(s.getContentAngle())
                    .targetAudience(s.getTargetAudience())
                    .hookDirection(s.getHookDirection())
                    .ctaDirection(s.getCtaDirection())
                    .status(CalendarItemStatus.PLANNED)
                    .build();
            newItems.add(itemRepo.save(item));
        }

        log.info("Calendar {} planning complete — {} items created", calendarId, newItems.size());
        return newItems.stream().map(ContentCalendarDto::toItemResponse).toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ContentCalendar getOrThrow(UUID id) {
        return calendarRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ContentCalendar", id));
    }

    private void assertCalendarExists(UUID calendarId) {
        if (!calendarRepo.existsById(calendarId)) {
            throw new ResourceNotFoundException("ContentCalendar", calendarId);
        }
    }

    private Map<String, Object> brandToMap(BrandProfile b) {
        return Map.of(
                "brandName", b.getBrandName(),
                "slogan", nullToEmpty(b.getSlogan()),
                "toneOfVoice", nullToEmpty(b.getToneOfVoice()),
                "targetAudience", nullToEmpty(b.getTargetAudience()),
                "keyMessages", b.getKeyMessages() != null ? b.getKeyMessages() : List.of(),
                "prohibitedClaims", b.getProhibitedClaims() != null ? b.getProhibitedClaims() : List.of()
        );
    }

    private List<Object> productsToList(List<ProductFact> products) {
        List<Object> result = new ArrayList<>();
        for (ProductFact p : products) {
            result.add(Map.of(
                    "productName", p.getProductName(),
                    "sku", nullToEmpty(p.getSku()),
                    "ply", p.getPly(),
                    "sheetCount", p.getSheetCount(),
                    "packSize", p.getPackSize(),
                    "cartonSize", p.getCartonSize(),
                    "keyBenefits", p.getKeyBenefits() != null ? p.getKeyBenefits() : List.of(),
                    "proofPoints", p.getProofPoints() != null ? p.getProofPoints() : List.of()
            ));
        }
        return result;
    }

    private String nullToEmpty(String s) {
        return s != null ? s : "";
    }
}
