package com.mst.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Action {
    private Long id;              // מזהה הפעולה המקורי (לתיעוד ולוגים ב-Processor)
    private String condition;     // מטריצת התנאים כטקסט JSON (למשל "[[1,2],[3]]")
    private ActionType action_type;   // סוג הפעולה (SMS או EMAIL)
    private String to;            // נמען: כתובת המייל או מספר הטלפון
    private String message;       // תוכן ההודעה שתישלח
}
