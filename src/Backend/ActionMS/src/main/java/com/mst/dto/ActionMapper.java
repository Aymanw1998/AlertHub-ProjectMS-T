package com.mst.dto;

import com.mst.model.Action;

public class ActionMapper {
    public static Action toEntity(ActionKafkaDTO dto) {
        Action a  = new Action();
        /**
         * private Long id;              // מזהה הפעולה המקורי (לתיעוד ולוגים ב-Processor)
         *     private String condition;     // מטריצת התנאים כטקסט JSON (למשל "[[1,2],[3]]")
         *     private ActionType action_type;   // סוג הפעולה (SMS או EMAIL)
         *     private String to;            // נמען: כתובת המייל או מספר הטלפון
         *     private String message;       // תוכן ההודעה שתישלח
         */
        a.setId(dto.getId());
        a.setCondition(dto.getCondition());
        a.setAction_type(dto.getAction_type());
        a.setTo(dto.getTo());
        a.setMessage(dto.getMessage());
        return a;
    }
    public static ActionKafkaDTO toDTO(Action a) {
        ActionKafkaDTO dto = new ActionKafkaDTO();
        dto.setId(a.getId());
        dto.setCondition(a.getCondition());
        dto.setAction_type(a.getAction_type());
        dto.setTo(a.getTo());
        dto.setMessage(a.getMessage());

        return dto;
    }
}
