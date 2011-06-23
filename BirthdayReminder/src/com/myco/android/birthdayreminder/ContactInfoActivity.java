/**
 * 
 */
package com.myco.android.birthdayreminder;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * @author sunny
 *
 */
public class ContactInfoActivity extends Activity {
  
  public static final String DATA_KEY__CONTACT_ID = "data_key__contact_id";
  ContactsReader reader;
  BContact bContact;
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.contact_info_form);

    // Read contact info
    reader = new ContactsReader(this);
    String lookup_key = this.getIntent().getExtras().getString(DATA_KEY__CONTACT_ID); 
    bContact = reader.getBContact(lookup_key);
    
    // Set Photo
    ImageView contactPhotoView = (ImageView) findViewById(R.id.contact_photo);
    Bitmap photoBitmap = reader.openContactPhoto(bContact.getLookup_Key());
    if (photoBitmap != null) {
      contactPhotoView.setImageBitmap(photoBitmap);
    }
    
    // Set Name
    TextView contactNameView = (TextView) findViewById(R.id.contact_name);
    try {
      contactNameView.setText(bContact.getName());
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    // Add Button Handlers
    Button contactInfoButton = (Button) findViewById(R.id.contact_info);
    contactInfoButton.setOnClickListener(new OnClickListener() {
      
      @Override
      public void onClick(View v) {
        launchContactViewer();
      }
    });    
  }
  
  protected void launchContactViewer() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    Uri lookUpUri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, 
      this.bContact.getLookup_Key());
    intent.setData(lookUpUri);
    startActivity(intent);
  }
}
