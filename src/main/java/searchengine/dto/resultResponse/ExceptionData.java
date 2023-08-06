package searchengine.dto.resultResponse;

import lombok.Data;

@Data
public class ExceptionData {
    private Result result;
    private String error;
}
