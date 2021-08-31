package com.ibnux.smsgateway.data;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;

@Entity
public class LogLine {
    @Id
    public long id;
    public long time;
    public String date;
    public String message;
}
