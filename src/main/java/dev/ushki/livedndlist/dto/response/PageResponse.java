package dev.ushki.livedndlist.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {

  @Schema(description = "List of items in the current page")
  private List<T> content;

  @Schema(description = "Current page number (0-based)", example = "0")
  private int pageNumber;

  @Schema(description = "Number of items per page", example = "20")
  private int pageSize;

  @Schema(description = "Total number of items available", example = "100")
  private long totalElements;

  @Schema(description = "Total number of pages available", example = "5")
  private int totalPages;

  @Schema(description = "Is this the first page?", example = "true")
  private boolean first;

  @Schema(description = "Is this the last page?", example = "false")
  private boolean last;

  @Schema(description = "Is the page empty?", example = "false")
  private boolean empty;

  public static <T> PageResponse<T> of(Page<T> page) {
    return PageResponse.<T>builder()
        .content(page.getContent())
        .pageNumber(page.getNumber())
        .pageSize(page.getSize())
        .totalElements(page.getTotalElements())
        .totalPages(page.getTotalPages())
        .first(page.isFirst())
        .last(page.isLast())
        .empty(page.isEmpty())
        .build();
  }
}
