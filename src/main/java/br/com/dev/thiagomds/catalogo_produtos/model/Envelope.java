package br.com.dev.thiagomds.catalogo_produtos.model;

import br.com.dev.thiagomds.catalogo_produtos.enums.EventType;

public class Envelope {

    private EventType eventType;
    private String data;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }
}
