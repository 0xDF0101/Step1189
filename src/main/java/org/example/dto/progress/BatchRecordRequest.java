package org.example.dto.progress;

import java.util.List;

public record BatchRecordRequest(
        int bibleId,
        List<Integer> chapterNumbers
) {
}
