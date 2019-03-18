package sample.servicebus;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

public class CommandToSend {

    public CommandToSend() {
    }

    public CommandToSend(String data, Date date) {
        this.data = data;
        this.date = date;
    }

    private String data;
    private Date date;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("data", data)
                .append("date", date)
                .toString();
    }
}
