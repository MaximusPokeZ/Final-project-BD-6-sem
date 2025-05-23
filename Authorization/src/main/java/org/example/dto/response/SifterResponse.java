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
public class SifterResponse {

  private long id;
  private long filterId;
  private long ruleId;
  private String fieldName;
  private String filterFunctionName;
  private String filterValue;
}
