package org.example.coding_convention.project.model;

import lombok.Getter;


public class EditorDto {
    @Getter
    public static class Range {
        private Integer startLineNumber;
        private Integer startColumn;
        private Integer endLineNumber;
        private Integer endColumn;
    }

    @Getter
    public static class Code {
        private Integer senderId;
        private String text;
        private EditorDto.Range range;
        private String type;
    }

}
