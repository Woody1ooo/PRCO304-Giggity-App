package com.liamd.giggity_app;

/**
 * Created by liamd on 26/04/2017.
 */

public class Ticket
{
    private String ticketId;
    private int admissionQuantity;
    private String gigId;

    public Ticket(String ticketId, int admissionQuantity, String gigId)
    {
        this.ticketId = ticketId;
        this.admissionQuantity = admissionQuantity;
        this.gigId = gigId;
    }

    public String getTicketId()
    {
        return ticketId;
    }

    public void setTicketId(String ticketId)
    {
        this.ticketId = ticketId;
    }

    public int getAdmissionQuantity()
    {
        return admissionQuantity;
    }

    public void setAdmissionQuantity(int admissionQuantity)
    {
        this.admissionQuantity = admissionQuantity;
    }

    public String getGigId()
    {
        return gigId;
    }

    public void setGigId(String gigId)
    {
        this.gigId = gigId;
    }
}
