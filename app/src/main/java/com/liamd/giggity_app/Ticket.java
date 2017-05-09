package com.liamd.giggity_app;

/**
 * Created by liamd on 26/04/2017.
 */

public class Ticket
{
    private String ticketID;
    private long admissionQuantity;
    private String gigID;
    private String ticketStatus;

    public Ticket()
    {

    }

    public Ticket(String ticketID, long admissionQuantity, String gigID, String ticketStatus)
    {
        this.ticketID = ticketID;
        this.admissionQuantity = admissionQuantity;
        this.gigID = gigID;
        this.ticketStatus = ticketStatus;
    }

    public String getTicketID()
    {
        return ticketID;
    }

    public void setTicketID(String ticketID)
    {
        this.ticketID = ticketID;
    }

    public long getAdmissionQuantity()
    {
        return admissionQuantity;
    }

    public void setAdmissionQuantity(long admissionQuantity)
    {
        this.admissionQuantity = admissionQuantity;
    }

    public String getGigID()
    {
        return gigID;
    }

    public void setGigID(String gigID)
    {
        this.gigID = gigID;
    }

    public String getTicketStatus()
    {
        return ticketStatus;
    }

    public void setTicketStatus(String ticketStatus)
    {
        this.ticketStatus = ticketStatus;
    }
}
