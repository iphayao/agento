package com.bnpaper.agento.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiContentResponse {

    private String title;
    private String hook;
    private String body;
    private String callToAction;
    private String hashtags;

    public boolean isValid() {
        return body != null && !body.isBlank();
    }
}
