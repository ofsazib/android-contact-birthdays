/**
 * 
 */
package com.myco.android.birthdayreminder;

import java.util.Calendar;
import java.util.GregorianCalendar;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * @author sunny
 *
 */
public class CheckEventsService extends Service {

  private static final int EVENT_CHECK_HOUR_OF_DAY = 8;
  private static final int EVENT_CHECK_MINUTE_OF_DAY = 30;
  private static final long EVENTS_CHECK_INTERVAL = 24 * 60 * 60 * 1000; // Every 24 hours
  private static final long EVENTS_CHECK_INTERVAL_DEBUG = 3 * 60 * 1000; // Every 3 minutes

  private static final boolean DEBUG = false;
  
  private static final int EVENT_NOFITICATION_ID = 3215;


  /* (non-Javadoc)
   * @see android.app.Service#onBind(android.content.Intent)
   */
  @Override
  public IBinder onBind(Intent arg0) {
    // Cannot bind this service
    return null;
  }
  
  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    
    Log.d("DEBUG", "Alarm raised");
    
    // Check for events and notify if any
    checkBirthdayEventsAndNotify();
  }

  private void checkBirthdayEventsAndNotify() {
    try {
      long contactListSize = new ContactsReader(this).getUpcomingEventCount();
      if (contactListSize > 0) {
        // Get notification manager
        NotificationManager notificationManager = 
          (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        
        // Instantiate notification
        String tickerText = "Upcoming birthdays & anniversaries";
        Notification notification = new Notification(R.drawable.reminder_smiley_face, tickerText, 
          System.currentTimeMillis());;
          
        // Alert with sound and vibration
        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        
        // Create intent
        Context context = getApplicationContext();
        CharSequence contentTitle = "New Event Reminders";
        CharSequence contentText = Long.toString(contactListSize) + 
          " of your friends have birthdays / anniversaries in coming days";
        // Launch BirthdayReminderActivity upon click
        // TODO Show wait while contact is shown
        Intent notificationIntent = new Intent(this, BirthdayReminderActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);

        // Notify
        notificationManager.notify(EVENT_NOFITICATION_ID, notification);
      }
    } catch (Exception e) {
      Log.d("DEBUG", e.getMessage());
    }
  }

  public static void schedule(Context context) {
    // Get CheckEventsService pending intent
    final Intent intent = new Intent(context, CheckEventsService.class);
    final PendingIntent pending = PendingIntent.getService(context, 0, intent, 0);
    
    // Cancel previously set alarm by CheckEventsService
    final AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarm.cancel(pending);
    
    // Schedule new alarm, first trigger within 24 hours
    Calendar c = new GregorianCalendar();
    if (!DEBUG) {
      if (c.get(Calendar.HOUR_OF_DAY) > EVENT_CHECK_HOUR_OF_DAY || 
          (c.get(Calendar.HOUR_OF_DAY) == EVENT_CHECK_HOUR_OF_DAY && c.get(Calendar.MINUTE) >= EVENT_CHECK_MINUTE_OF_DAY)) {
        c.add(Calendar.DAY_OF_YEAR, 1);
      }
      c.set(Calendar.HOUR_OF_DAY, EVENT_CHECK_HOUR_OF_DAY);
      c.set(Calendar.MINUTE, EVENT_CHECK_MINUTE_OF_DAY);
      c.set(Calendar.SECOND, 0);
      c.set(Calendar.MILLISECOND, 0);
      alarm.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), EVENTS_CHECK_INTERVAL, pending);
    } else {
      alarm.setRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), EVENTS_CHECK_INTERVAL_DEBUG, pending);
    }
  }
}
