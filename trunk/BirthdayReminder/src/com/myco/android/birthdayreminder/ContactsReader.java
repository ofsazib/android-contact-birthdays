/**
 * 
 */
package com.myco.android.birthdayreminder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * @author sunny
 *
 */
public class ContactsReader {
  private static final int NUM_FORWARD_DAYS = 30;

  private Context context;
  
  //Extract in MM-DD format for comparison
  private String databaseDateFormat = getDatabaseDateFormat();
 
  public ContactsReader(Context context) {
    // Set context
    this.context = context;
  }

  private String getDateStringInMM_DD(Calendar date) {
    // Need in MM-DD format for comparison
    int month = date.get(Calendar.MONTH) + 1; // Month is somehow 0 based but day is 1 based
    int day = date.get(Calendar.DAY_OF_MONTH);
    return padZeroToDateField(Integer.toString(month)) + "-" + padZeroToDateField(Integer.toString(day));
  }
  
  private String padZeroToDateField(String string) {
    switch(string.length()) {
      case 1:
        return "0" + string;
      case 2:
        return string;
      default:
        // Invalid in the context
        return null;
    }
  }

  public long getUpcomingEventCount() throws Exception {
    return upcomingEvents().size();
  }
  
  public List<BContact> upcomingEvents() throws Exception {
    // Uniquefy and return
    // TODO Read preferences for NUM_FORWARD_DAYS
    return uniquefy(upcomingEvents(NUM_FORWARD_DAYS));
  }
  
  private List<BContact> upcomingEvents(int numDaysForward) throws Exception {
    // Check for negative numDaysForward
    if (numDaysForward < 0) {
      // TODO: Change type of exception thrown?
      throw new Exception("numDaysForward cannot be negative");
    }
    
    // Check if numDaysForward greater than an year away
    if (numDaysForward > 365) {
      // TODO: Change type of exception thrown?
      throw new Exception("numDaysForward cannot be greater than 365 as it'd start repeating data");
    }
    
    // Get string for today
    Calendar todaydate = Calendar.getInstance();
    String today = getDateStringInMM_DD(todaydate);
    
    // Add numDaysForward and get string for endday
    Calendar enddate = Calendar.getInstance();
    enddate.add(Calendar.DATE, numDaysForward);
    String endday = getDateStringInMM_DD(enddate);
    
    // Check if year changes in between
    int todayYear = todaydate.get(Calendar.YEAR);
    int endYear = enddate.get(Calendar.YEAR);
    
    List<BContact> list;
    if (endYear > todayYear) {
      // Get middates in MM-DD format for comparison
      String currentYearEndDay = "12-31";
      String nextYearStartDay = "01-01";
      
      // Get parts and join
      list = upcomingEvents(today, currentYearEndDay);
      // Update num year old...
      updateNumYearOld(list, todayYear);
      
      List<BContact> list2 = upcomingEvents(nextYearStartDay, endday);
      // Update num year old...
      updateNumYearOld(list, todayYear);
      
      list.addAll(list2);      
    } else {
      // Same year, just get and return
      list = upcomingEvents(today, endday);
      // Update num year old...
      updateNumYearOld(list, todayYear);
    }

    return list;
  }

  /**
   * @param list
   * @return
   * @throws Exception 
   */
  private List<BContact> uniquefy(List<BContact> list) throws Exception {
    // First Unique by ID, as there can be multiple entires for the same event. Logic to idenfity unique items is in BContact.hashcode...
    // Need to clean duplicates which only differ in # of years...
    HashSet<BContact> set = new HashSet<BContact>();
    for(BContact con : list) {
      // BContact.hashcode returns same code if ID, event type and day / month is same, and if duplicate, add will fail
      if (!set.add(con)) {
        // Check if current event has a valid event_num_year_old, and if so, replace
        if (con.getEvent_num_year_old() != -1) {
          // Remove the one with same hashcode
          if (set.remove(con)) {
            // Add more appropriate object, note that earlier object may also be as appropriate
            if (!set.add(con)) {
              // TODO Error
              throw new Exception("Adding more appropriate event failed: " + con.getLookup_Key());
            }
          } else {
            // TODO Error
            throw new Exception("Removing existing event failed: " + con.getLookup_Key());
          }
        }
      }
    }
    
    // Get list and sort
    ArrayList<BContact> uniqueList = new ArrayList<BContact>(set);
    Collections.sort(uniqueList);
    return uniqueList;
  }

  private void updateNumYearOld(List<BContact> list, int todayYear) {
    for (BContact con : list) {
      if (con.getEvent_date_year() > 0) {
        con.setEvent_num_year_old(todayYear - con.getEvent_date_year());
      }
    }
  }

  private List<BContact> upcomingEvents(String fromDate, String toDate) throws Exception {
    // Get Uri for contact content provider
    Uri dataUri = ContactsContract.Data.CONTENT_URI;
    
    // Fields to fetch in query
    String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME,                         
              ContactsContract.Contacts.LOOKUP_KEY,
              ContactsContract.CommonDataKinds.Event.START_DATE,
              ContactsContract.CommonDataKinds.Event.TYPE,
              };

    // Query selection
    String selection = ContactsContract.Data.MIMETYPE + "= ? AND " + 
      extractDateInMM_DD(ContactsContract.CommonDataKinds.Event.START_DATE) + " >= ?" + 
      " AND " + extractDateInMM_DD(ContactsContract.CommonDataKinds.Event.START_DATE) + " <= ?";
    
    // Arguments to query above
    String[] selectArgs = new String[] {ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, fromDate, toDate};
    
    // We don't need to sort here as sorting will be done again after fetching
    String sortOrder = null; /* extractDateInMM_DD(ContactsContract.CommonDataKinds.Event.START_DATE); */
    
    // Search into a Cursor
    Cursor c = this.context.getContentResolver().query(dataUri, projection, 
      selection, selectArgs, sortOrder);

    // Convert results into List<BContact> and return
    List<BContact> result = new ArrayList<BContact>();
    while (c.moveToNext()) {
      String dateString = c.getString(2);
      // TODO Error handling
      int day = getDatabaseDateDay(dateString);
      int month = getDatabaseDateMonth(dateString);
      int year = getDatabaseDateYear(dateString);
      
      String name = c.getString(0);
      String lookup_key = c.getString(1);
      String type = c.getString(3);
      BContact contact = new BContact(name, lookup_key, 
        day, month, year, Integer.parseInt(type));
      result.add(contact);
    }
    c.close();
    return result;
  }

  /**
   * @param string
   * @return
   */
  private int getDatabaseDateYear(String string) {
    int index = this.databaseDateFormat.indexOf("yyyy");
    String str = string.substring(index, index + 4);
    return Integer.parseInt(str);
  }

  /**
   * @param string
   * @return
   */
  private int getDatabaseDateMonth(String string) {
    int index = this.databaseDateFormat.indexOf("mm");
    String str = string.substring(index, index + 2);
    return Integer.parseInt(str) - 1;
  }

  /**
   * @param string
   * @return
   */
  private int getDatabaseDateDay(String string) {
    int index = this.databaseDateFormat.indexOf("dd");
    String str = string.substring(index, index + 2);
    return Integer.parseInt(str);
  }

  private String extractDateInMM_DD(String field) throws Exception {
    // SQL index is 1 based
    int mm_index = this.databaseDateFormat.indexOf("mm") + 1;
    int dd_index = this.databaseDateFormat.indexOf("dd") + 1;

    if (mm_index == 0 || dd_index == 0) {
      throw new Exception("Badly formed Database Date Format");
    }
    
    return "(substr(" + field + "," + Integer.toString(mm_index) + ",2) || \"-\" || substr(" + field + "," + Integer.toString(dd_index) + ",2))";
  }
  
  private String getDatabaseDateFormat() {
    // Not yet needed till Issue #9676 & #2947 are fixed - currently the database format is yyyy-mm-dd
    return "yyyy-mm-dd";
  }
  
  public Bitmap openContactPhoto(String lookup_key) {
    // Find contentUri since only that seems to work
    Uri lookUpUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, lookup_key);
    Uri contentUri = ContactsContract.Contacts.lookupContact(this.context.getContentResolver(), 
      lookUpUri);

    InputStream stream = null;
    try {
      stream = ContactsContract.Contacts.openContactPhotoInputStream(this.context.getContentResolver(),
          contentUri);
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    if (stream != null) {
      return BitmapFactory.decodeStream(stream);
    } else {
      return null;
    }
  }

  /**
   * @param string
   * @return
   */
  public BContact getBContact(String lookup_key) {
    // Uri
    Uri dataUri = ContactsContract.Data.CONTENT_URI;
    
    // Fields to fetch in query
    String[] projection = new String[] { ContactsContract.Contacts.DISPLAY_NAME,     
              ContactsContract.CommonDataKinds.Event.START_DATE,
              ContactsContract.CommonDataKinds.Event.TYPE,
              };
    
    // Query selection
    String selection = ContactsContract.Data.MIMETYPE + "= ? AND " + ContactsContract.Contacts.LOOKUP_KEY + "= ?";
    
    // Arguments to query above
    String[] selectArgs = new String[] {ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, lookup_key};
    
    // Search into a Cursor
    Cursor c = null;
    BContact result = null;
    try {
      c = this.context.getContentResolver().query(dataUri, projection, selection, selectArgs, null);
    
      // Convert results into BContact and return
      // Count can be greater than 1, in that case need to uniquefy
      List<BContact> resultList = new ArrayList<BContact>();
      while (c.moveToNext()) {
        // Parse data
        String name = c.getString(0);
        String type = c.getString(2);
        String dateString = c.getString(1);
        int day = getDatabaseDateDay(dateString);
        int month = getDatabaseDateMonth(dateString);
        int year = getDatabaseDateYear(dateString);
        
        BContact con = new BContact(name, lookup_key, 
          day, month, year, Integer.parseInt(type));
        resultList.add(con);
      }
      
      // Uniquefy if more than 1
      if (resultList.size() != 1) {
        resultList = uniquefy(resultList);
        if (resultList.size() != 1) {
          // TODO something is wrong
        }
      }
      
      result = resultList.get(0);
    } catch (Exception e) {
      // TODO Error handling?
      e.printStackTrace();
    } finally {
      if (c != null) {
        c.close();
      }
    }
   
    return result;
  }
}
