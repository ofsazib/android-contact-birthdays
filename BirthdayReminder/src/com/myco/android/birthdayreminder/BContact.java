/**
 * 
 */
package com.myco.android.birthdayreminder;

import android.provider.ContactsContract;

/**
 * @author sunny
 *
 */
class BContact implements Comparable<BContact> {
  private String name, contact_id;
  private int event_type;
  private int event_date_day, event_date_month, event_date_year;
  private int event_num_year_old;
  
  public static final int BIRTHDAY_EVENT = ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY;
  public static final int ANNIVERSARY_EVENT = ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY;
  public static final int OTHER_EVENT = -1;

  public BContact(String name, String contact_id, int event_date_day, int event_date_month, int event_date_year, int event_type) {
    this.name = name;
    this.contact_id = contact_id;
    this.event_date_day = event_date_day;
    this.event_date_month = event_date_month;
    this.event_date_year = event_date_year;
    this.event_type = event_type;
    
    // Initialize num_year_old as invalid
    this.setEvent_num_year_old(-1);
  }

  /**
   * @return the name
   */
  public String getName() {
    return this.name;
  }

  /**
   * @return the contact_id
   */
  public String getContact_id() {
    return this.contact_id;
  }

  /**
   * @return the event_date_day
   */
  public int getEvent_date_day() {
    return this.event_date_day;
  }

  /**
   * @return the event_date_month
   */
  public int getEvent_date_month() {
    return this.event_date_month;
  }

  /**
   * @return the event_date_year
   */
  public int getEvent_date_year() {
    return this.event_date_year;
  }

  /**
   * @return the event_num_year_old
   */
  public int getEvent_num_year_old() {
    return this.event_num_year_old;
  }

  /**
   * @param event_num_year_old the event_num_year_old to set
   */
  public void setEvent_num_year_old(int event_num_year_old) {
    this.event_num_year_old = event_num_year_old;
  }

  public int getEventType() {
    switch (this.event_type) {
      case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY:
        return BIRTHDAY_EVENT;
      case ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY:
        return ANNIVERSARY_EVENT;
      default:
        return OTHER_EVENT;    
    }
  }

  /* (non-Javadoc)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.contact_id == null) ? 0 : this.contact_id.hashCode());
    result = prime * result + this.event_date_day;
    result = prime * result + this.event_date_month;
    result = prime * result + this.event_type;
    return result;
  }

  /* (non-Javadoc)
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (!(obj instanceof BContact))
      return false;
    BContact other = (BContact) obj;
    if (this.contact_id == null) {
      if (other.contact_id != null)
        return false;
    } else if (!this.contact_id.equals(other.contact_id))
      return false;
    if (this.event_date_day != other.event_date_day)
      return false;
    if (this.event_date_month != other.event_date_month)
      return false;
    if (this.event_type != other.event_type)
      return false;
    return true;
  }

  /* (non-Javadoc)
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  @Override
  public int compareTo(BContact arg0) {
    final int BEFORE = -1;
    final int EQUAL = 0;
    final int AFTER = 1;

    //this optimization is usually worthwhile, and can
    //always be added
    if ( this == arg0 ) return EQUAL;
    
    if (this.event_date_month < arg0.event_date_month) { 
      return BEFORE;
    } else {
      if (this.event_date_month > arg0.event_date_month) { 
        return AFTER;
      } else {
        // Months have to be equal now
        if (this.event_date_day < arg0.event_date_day) {
          return BEFORE;
        } else {
          if (this.event_date_day > arg0.event_date_day) {
            return AFTER;
          }
          // Days have to be equal, sort by name
          return this.name.compareTo(arg0.name);
        }
      }
    }
  }
}
