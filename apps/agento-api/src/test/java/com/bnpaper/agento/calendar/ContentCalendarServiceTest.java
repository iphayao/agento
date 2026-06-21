package com.bnpaper.agento.calendar;

import com.bnpaper.agento.brand.BrandProfile;
import com.bnpaper.agento.brand.BrandProfileRepository;
import com.bnpaper.agento.common.exception.ResourceNotFoundException;
import com.bnpaper.agento.product.ProductFactRepository;
import com.bnpaper.agento.workflow.WorkerClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContentCalendarServiceTest {

    @Mock private ContentCalendarRepository calendarRepo;
    @Mock private CalendarItemRepository itemRepo;
    @Mock private BrandProfileRepository brandRepo;
    @Mock private ProductFactRepository productRepo;
    @Mock private WorkerClient workerClient;

    @InjectMocks
    private ContentCalendarService service;

    private UUID calendarId;
    private ContentCalendar calendar;

    @BeforeEach
    void setUp() {
        calendarId = UUID.randomUUID();
        calendar = ContentCalendar.builder()
                .id(calendarId)
                .name("June 2026 Calendar")
                .periodStart(LocalDate.of(2026, 6, 1))
                .periodEnd(LocalDate.of(2026, 6, 30))
                .objective("Drive TikTok sales")
                .status(CalendarStatus.DRAFT)
                .build();
    }

    @Test
    void createCalendar_savesAndReturnsResponse() {
        when(calendarRepo.save(any())).thenReturn(calendar);

        ContentCalendarDto.CreateRequest req = new ContentCalendarDto.CreateRequest();
        req.setName("June 2026 Calendar");
        req.setPeriodStart(LocalDate.of(2026, 6, 1));
        req.setPeriodEnd(LocalDate.of(2026, 6, 30));
        req.setObjective("Drive TikTok sales");

        ContentCalendarDto.CalendarResponse resp = service.createCalendar(req);

        assertThat(resp.getName()).isEqualTo("June 2026 Calendar");
        assertThat(resp.getStatus()).isEqualTo("DRAFT");
        assertThat(resp.getItemCount()).isEqualTo(0);
        verify(calendarRepo).save(any());
    }

    @Test
    void createCalendar_throwsWhenEndBeforeStart() {
        ContentCalendarDto.CreateRequest req = new ContentCalendarDto.CreateRequest();
        req.setName("Bad Calendar");
        req.setPeriodStart(LocalDate.of(2026, 6, 30));
        req.setPeriodEnd(LocalDate.of(2026, 6, 1));

        assertThatThrownBy(() -> service.createCalendar(req))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void findCalendar_throwsWhenNotFound() {
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findCalendar(calendarId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listCalendars_returnsAllWithItemCount() {
        when(calendarRepo.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(calendar));
        when(itemRepo.countByCalendarId(calendarId)).thenReturn(5);

        List<ContentCalendarDto.CalendarResponse> result = service.listCalendars();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getItemCount()).isEqualTo(5);
    }

    @Test
    void addItem_savesItemWithPlannedStatus() {
        when(calendarRepo.existsById(calendarId)).thenReturn(true);
        CalendarItem saved = CalendarItem.builder()
                .id(UUID.randomUUID())
                .calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 10))
                .channel("tiktok")
                .status(CalendarItemStatus.PLANNED)
                .build();
        when(itemRepo.save(any())).thenReturn(saved);

        ContentCalendarDto.ItemCreateRequest req = new ContentCalendarDto.ItemCreateRequest();
        req.setPlannedDate(LocalDate.of(2026, 6, 10));
        req.setChannel("tiktok");
        req.setContentType("TIKTOK_CAPTION");
        req.setContentAngle("ฝุ่นน้อย เหมาะสำหรับออฟฟิศ");

        ContentCalendarDto.ItemResponse resp = service.addItem(calendarId, req);

        assertThat(resp.getStatus()).isEqualTo("PLANNED");
        assertThat(resp.getChannel()).isEqualTo("tiktok");
        verify(itemRepo).save(any());
    }

    @Test
    void addItem_throwsWhenCalendarNotFound() {
        when(calendarRepo.existsById(calendarId)).thenReturn(false);

        ContentCalendarDto.ItemCreateRequest req = new ContentCalendarDto.ItemCreateRequest();
        req.setPlannedDate(LocalDate.of(2026, 6, 10));
        req.setChannel("tiktok");

        assertThatThrownBy(() -> service.addItem(calendarId, req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateItem_patchesFields() {
        UUID itemId = UUID.randomUUID();
        CalendarItem existing = CalendarItem.builder()
                .id(itemId)
                .calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 10))
                .channel("tiktok")
                .status(CalendarItemStatus.PLANNED)
                .build();
        when(calendarRepo.existsById(calendarId)).thenReturn(true);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(existing));
        when(itemRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ContentCalendarDto.ItemUpdateRequest req = new ContentCalendarDto.ItemUpdateRequest();
        req.setChannel("shopee");
        req.setContentAngle("คุ้มค่าสำหรับบ้าน");

        ContentCalendarDto.ItemResponse resp = service.updateItem(calendarId, itemId, req);

        assertThat(resp.getChannel()).isEqualTo("shopee");
        assertThat(resp.getContentAngle()).isEqualTo("คุ้มค่าสำหรับบ้าน");
    }

    @Test
    void deleteCalendar_throwsWhenNotFound() {
        when(calendarRepo.existsById(calendarId)).thenReturn(false);

        assertThatThrownBy(() -> service.deleteCalendar(calendarId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void planCalendar_callsWorkerAndSavesItems() {
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(brandRepo.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(
                BrandProfile.builder().id(UUID.randomUUID()).brandName("SoClean")
                        .keyMessages(List.of()).prohibitedClaims(List.of()).build()));
        when(productRepo.findAll()).thenReturn(List.of());
        when(itemRepo.findByCalendarIdOrderByPlannedDateAsc(calendarId)).thenReturn(List.of());

        ContentCalendarDto.PlanWorkerResponse planResp = new ContentCalendarDto.PlanWorkerResponse();
        ContentCalendarDto.ItemSuggestion suggestion = new ContentCalendarDto.ItemSuggestion();
        suggestion.setPlannedDate("2026-06-10");
        suggestion.setChannel("tiktok");
        suggestion.setContentType("TIKTOK_CAPTION");
        suggestion.setContentAngle("ฝุ่นน้อย");
        suggestion.setTargetAudience("Women Gen Y");
        suggestion.setHookDirection("ทำไมต้องใช้ทิชชู่ที่ดี");
        suggestion.setCtaDirection("สั่งได้เลย");
        planResp.setSuggestions(List.of(suggestion));

        when(workerClient.planCalendar(any())).thenReturn(planResp);
        CalendarItem savedItem = CalendarItem.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .plannedDate(LocalDate.of(2026, 6, 10)).channel("tiktok")
                .status(CalendarItemStatus.PLANNED).build();
        when(itemRepo.save(any())).thenReturn(savedItem);

        List<ContentCalendarDto.ItemResponse> items = service.planCalendar(calendarId);

        assertThat(items).hasSize(1);
        assertThat(items.get(0).getChannel()).isEqualTo("tiktok");
        verify(workerClient).planCalendar(any());
    }

    @Test
    void planCalendar_throwsWhenNoBrandProfile() {
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(brandRepo.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.planCalendar(calendarId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("brand profile");
    }

    @Test
    void planCalendar_throwsWhenWorkerReturnsNoSuggestions() {
        when(calendarRepo.findById(calendarId)).thenReturn(Optional.of(calendar));
        when(brandRepo.findFirstByOrderByCreatedAtAsc()).thenReturn(Optional.of(
                BrandProfile.builder().id(UUID.randomUUID()).brandName("SoClean")
                        .keyMessages(List.of()).prohibitedClaims(List.of()).build()));
        when(productRepo.findAll()).thenReturn(List.of());

        ContentCalendarDto.PlanWorkerResponse emptyResp = new ContentCalendarDto.PlanWorkerResponse();
        emptyResp.setSuggestions(List.of());
        when(workerClient.planCalendar(any())).thenReturn(emptyResp);

        assertThatThrownBy(() -> service.planCalendar(calendarId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no suggestions");
    }
}
