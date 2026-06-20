package com.bnpaper.agento.ai;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AiContentRequest {

    private String brandName;
    private String slogan;
    private String toneOfVoice;
    private String brandTargetAudience;
    private List<String> keyMessages;
    private List<String> prohibitedClaims;

    private String productName;
    private Integer sheetCount;
    private Integer ply;
    private Integer packSize;
    private Integer cartonSize;
    private List<String> keyBenefits;
    private List<String> proofPoints;

    private String campaignName;
    private String campaignObjective;
    private String channel;
    private String campaignTargetAudience;
    private String contentAngle;
    private String contentType;
    private String promptVersion;
}
