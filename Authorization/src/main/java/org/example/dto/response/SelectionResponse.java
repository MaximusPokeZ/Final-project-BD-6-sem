package org.example.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SelectionResponse {

  private long id;
  private long deduplicationId;
  private long ruleId;
  private String fieldName;
  private long timeToLiveSec;
  private boolean isActive;
}
