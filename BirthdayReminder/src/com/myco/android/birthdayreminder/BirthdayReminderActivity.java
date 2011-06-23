package com.myco.android.birthdayreminder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

public class BirthdayReminderActivity extends ListActivity {

  private List<BContact> contactsList;
  
  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Schedule notification
    CheckEventsService.schedule(this);

    // TODO change presentation
    ContactsReader reader = new ContactsReader(this);
    try {
      // Update list
      contactsList = reader.upcomingEvents();
      setListAdapter(new IconicAdapter(this));

      ListView lv = getListView();
      lv.setTextFilterEnabled(true);

      // Add item click handler
      lv.setOnItemClickListener(new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
            long id) {
          showContactInfo(position);
        }
      });

    } catch (Exception e) {
      // TODO Error handling
      e.printStackTrace();
    }
  }

  /**
   * @param position
   */
  protected void showContactInfo(int position) {
    Intent intent = new Intent(this, ContactInfoActivity.class);
    BContact con = this.contactsList.get(position);
    intent.putExtra(ContactInfoActivity.DATA_KEY__CONTACT_ID, con.getLookup_Key());
    startActivity(intent);
  }

  private List<String> getContactNameArray() {
    ArrayList<String> strlist = new ArrayList<String>();
    for (BContact con : this.contactsList) {
      strlist.add(getDisplayMessage(con));
    }

    return strlist;
  }
  
  private String getDisplayMessage(BContact con) {
    String message;
    switch (con.getEventType()) {
      case BContact.BIRTHDAY_EVENT:
        // Check for valid age...
        if (con.getEvent_num_year_old() != -1) {
          message = con.getName() + " turns " + con.getEvent_num_year_old() + " " + getDayOfMonth(con);
        } else {
          message = con.getName() + "'s Birthday " + getDayOfMonth(con);
        }
        break;
      case BContact.ANNIVERSARY_EVENT:
        // Check for valid age...
        if (con.getEvent_num_year_old() != -1) {
          message = con.getName() + "'s " + getNumberText(con.getEvent_num_year_old()) + " Anniversary " + getDayOfMonth(con);
        } else {
          message = con.getName() + "'s Anniversary " + getDayOfMonth(con);
        }
        break;
      default:
        message = con.getName() + ", " + getDayOfMonth(con);
    }
    
    return message;
  }

  private String getNumberText(int number) {
    // Get unit digit
    int unit_digit = number % 10;
    
    // Add text
    if (unit_digit == 0 || unit_digit >= 4) {
      return Integer.toString(number) + "th";
    }
    if (unit_digit == 2) {
      return Integer.toString(number) + "nd";
    }
    if (unit_digit == 3) {
      return Integer.toString(number) + "rd";
    }
    
    // This will never hit
    return null;
  }

  public String getDayOfMonth(BContact con) {
    Calendar today = Calendar.getInstance();
    
    if (today.get(Calendar.DAY_OF_MONTH) == con.getEvent_date_day() 
      && today.get(Calendar.MONTH) == con.getEvent_date_month()) {
      return "today";
    }
    
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MMM-d");
    
    // Check for invalid year
    int year = con.getEvent_date_year();
    if (year == 0) {
      // Need a valid year here...
      year = 1900;
    } 
      
    return "on " + dateFormatter.format(new Date(year, con.getEvent_date_month(), con.getEvent_date_day()));
  }
  
  private int getEventType(int position) {
    return this.contactsList.get(position).getEventType();
  }
  
  class IconicAdapter extends ArrayAdapter<String> {

    /**
     * @param context
     * @param textViewResourceId
     */
    public IconicAdapter(Context context) {
      super(context, R.layout.list_icon_text_item, R.id.label, getContactNameArray());
    }
    
    /* (non-Javadoc)
     * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      
      // Optimize using recycled rows - may not help since we need to regenerate display message
      View row = super.getView(position, convertView, parent);
      ImageView icon = (ImageView) row.findViewById(R.id.icon);
      
      // Select icon image
      switch (getEventType(position)) {
        case BContact.BIRTHDAY_EVENT:
          icon.setImageResource(R.drawable.ic_birthday);
          break;
        case BContact.ANNIVERSARY_EVENT:
          icon.setImageResource(R.drawable.ic_anniversary);
          break;
        default:
          // TODO change to generic event icon
          icon.setImageResource(R.drawable.icon);
      }
      
      return row;
    }
  }
}