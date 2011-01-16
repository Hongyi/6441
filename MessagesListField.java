/*
 * MessagesListField.java
 *
 * Copyright © 1998-2010 Research In Motion Ltd.
 * 
 * Note: For the sake of simplicity, this sample application may not leverage
 * resource bundles and resource strings.  However, it is STRONGLY recommended
 * that application developers make use of the localization features available
 * within the BlackBerry development platform to ensure a seamless application
 * experience across a variety of languages and geographies.  For more information
 * on localizing your application, please refer to the BlackBerry Java Development
 * Environment Development Guide associated with this release.
 */

package com.rim.samples.device.blackberrymaildemo;

import net.rim.device.api.ui.component.*;
import net.rim.device.api.i18n.DateFormat;
import net.rim.device.api.ui.*;
import net.rim.device.api.system.*;
import java.util.*;

import net.rim.blackberry.api.mail.*;

/**
 * This class serves as the display for the MessageViewScreen. It handles the
 * drawing of the list of messages, displaying the status, sender/recipient and
 * the subject of the message. The MessagesListField class also handles its own
 * ListFieldCallback actions.
 */
public final class MessagesListField extends ListField implements ListFieldCallback
{
    private static final int FIRST = 0;    
    
    private int _statusColumnWidth;    
    private int _dateColumnWidth;
    private int _nameColumnWidth;

    private Vector _messages;
    

    /**
     * Contructs a new MessagesListField
     * @param messages The messages to be displayed in the list
     */
    public MessagesListField(Vector messages)
    {
        super();

        _messages = messages;

        setCallback(this);
        setSize(messages.size());
    }
       

    /**
     * Gets the sender's name. This method tries to extract the common name. If
     * the common name is not available then the email account name is returned 
     * instead. If this method cannot retrieve either the common name or email 
     * account name then it returns MessageScreen.UNKNOWN_NAME.
     * 
     * @param message The message to extract the sender's name from
     * @return The sender's name
     */
    private String getSenderName(Message message)
    {
        try
        {
            // Extract the sender's address
            Address address = message.getFrom();
            if( address != null )
            {
                // If the name isn't null retrieve the sender's first name
                String name = address.getName();
                if( name != null && name.length() > 0 )
                {
                    int spaceIndex = name.indexOf(" ");
                    if( spaceIndex > 0 ) // There is a first name
                    {
                        return name.substring(0, spaceIndex);
                    }

                    return name;
                }

                // If there is no name to display then display the email
                // account name.
                name = address.getAddr();
                int atIndex = name.indexOf("@");
                if( atIndex != -1 )
                {
                    return name.substring(0, atIndex);
                }
            }
        }
        catch( MessagingException e )
        {
            BlackBerryMailDemo.errorDialog(e.toString());
        }

        return MessageScreen.UNKNOWN_NAME; // The name could not be found
    }
    
   
    /**
     * Gets the first recipient's name. This method tries to extract the common 
     * name. If the common name is not available then the email account name is 
     * returned instead. If this method cannot retrieve either the common name 
     * or email account name then it returns MessageScreen.UNKNOWN_NAME.
     * 
     * @param message The message to extract the recipient's name from
     * @return The recipient's name
     */ 
    private String getFirstRecipientName(Message message)
    {
        try
        {
            Address[] recipients = message.getRecipients(Message.RecipientType.TO);
            if( recipients != null && recipients.length > 0 )
            {
                String name = recipients[ FIRST ].getName();

                // If the name does not exist then use the email account name
                if( name == null || name.length() == 0 )
                {
                    name = recipients[ FIRST ].getAddr();
                    int atIndex = name.indexOf("@");
                    if( atIndex != -1 )
                    {
                        return name.substring(0, atIndex);
                    }
                }

                return name;
            }
        }
        catch( MessagingException e )
        {
            BlackBerryMailDemo.errorDialog("Message#getRecipients(int) threw " + e.toString());
        }

        return MessageScreen.UNKNOWN_NAME;
    }
    
    
    /**
     * @see ListField#layout(int, int)
     */
    protected void layout(int width, int height)
    {   
        // Determine column widths
        _statusColumnWidth = width / 16;    
        _dateColumnWidth = (int) (width * 0.26);
        _nameColumnWidth = (int) (width * 0.27);
        
        super.layout(width, height);
    }
    

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#drawListRow(ListField, Graphics, int, int, int)
     */
    public void drawListRow(ListField list, Graphics g, int index, int y, int w)
    {        
        Message message = (Message) _messages.elementAt(index);

        int x = 0; 

        // If an icon associated with the message's status exists in our
        // _statusMap, then draw it.
        g.drawText(Util.getStatusIcon(message), x, y, 0, w);        

        x += _statusColumnWidth;

        // Display the date that the message was received
        Date recievedDate = message.getReceivedDate();
        g.drawText(Util.getDateAsString(recievedDate, DateFormat.DATE_SHORT), x, y, 0, _dateColumnWidth);
        x += _dateColumnWidth;

        // Display the name of the sender if the message is inbound
        String name;
        if( message.isInbound() )
        {
            name = getSenderName(message);
        }
        else // Outbound message
        {
            name = getFirstRecipientName(message);
        }
        g.drawText(name, x, y, Graphics.ELLIPSIS, _nameColumnWidth);
        x += _nameColumnWidth;

        // Display the subject in the remaining column width
        int remainingColumnWidth = Display.getWidth() - x;
        String textToDisplay = message.getSubject();
        if( textToDisplay == null ) // No subject
        {
            textToDisplay = MessageScreen.NO_SUBJECT;
        }

        g.drawText(textToDisplay, x, y, Graphics.ELLIPSIS, remainingColumnWidth);
    }
    

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#get(ListField , int)
     */
    public Object get(ListField list, int index)
    {
        return _messages.elementAt(index);
    }
    

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#indexOfList(ListField, String, int)
     */
    public int indexOfList(ListField list, String p, int s)
    {
        return -1; // Not supported
    }
    

    /**
     * @see net.rim.device.api.ui.component.ListFieldCallback#getPreferredWidth(ListField)
     */
    public int getPreferredWidth(ListField list)
    {
        return Display.getWidth();
    }
}
