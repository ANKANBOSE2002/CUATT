package com.example.CUATT;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.icu.text.DateFormat;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Date;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    //Intialize attributes
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    TextView attendanceTextView;
    Map<String, String> nfcUserMap;
    ArrayList<String> presentStudents;
    TextView presentStudentsTextView;
    final static String TAG = "nfc_test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialise NfcAdapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        //If no NfcAdapter, display that the device has no NFC
        if (nfcAdapter == null) {
            Toast.makeText(this, "NO NFC Capabilities",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, this.getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        attendanceTextView = findViewById(R.id.textview);
        nfcUserMap = new HashMap<>();
        presentStudents = new ArrayList<>();
        nfcUserMap.put("5a 91 ca ce", "Ankan Bose 20BCS3576");
        nfcUserMap.put("59 6d 58 be", "Vibhor Mishra 20BCS3680");
        nfcUserMap.put("db 91 22 7a", "Aayush Somankar 20BCS3641");
        nfcUserMap.put("dc b1 28 6a", "Palak Dawar 20BCS3696");
        nfcUserMap.put("5a 86 5a de", "Rakshita Pradhan 20BCS3679");
        nfcUserMap.put("5a 88 c2 5e", "Vinit Kundu 20BCS36367");
    }

    @Override
    protected void onResume() {
        super.onResume();
        assert nfcAdapter != null;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
    }

    protected void onPause() {
        super.onPause();
        //Onpause stop listening
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        resolveIntent(intent);
    }

    private void resolveIntent(Intent intent) {
            String action = intent.getAction();
            if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                    || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
                Tag tag = (Tag) intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                assert tag != null;
                byte[] payload = detectTagData(tag).getBytes();
                // Get the NFC ID as a hex string
                String nfcId = toHex(tag.getId());

                // Get the username associated with the NFC ID
                String username = nfcUserMap.get(nfcId);

                if (username != null) {
                    // Update attendance TextView with the scanned tag's username


                    // Check if the student is already present
                    String currentTime = null;
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        currentTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date());

                    }
                    String wtime = username + " " + currentTime;
                    int l = wtime.length();
                    String jjs = wtime.substring(l-8,l);
                    if (!presentStudents.contains(wtime)) {
                        // Add the present student to the array list
                        attendanceTextView.setText("Marked attendance: \n" + username);

                        presentStudents.add(wtime);

                        // Display the list of present students
                        LinearLayout attendanceList = findViewById(R.id.attendance_list);
                        attendanceList.removeAllViews();
                        for (int i = 0; i < presentStudents.size(); i++) {
                            TextView textView = new TextView(this);
                            textView.setText(presentStudents.get(i));
                            textView.setTextSize(18);
                            textView.setTextColor(Color.BLACK);
                            attendanceList.addView(textView);
                        }
                    } else {
                        // Show toast message for duplicate entry
                        Toast.makeText(this, "This student has already been marked present", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    attendanceTextView.setText("ID Not Registered");
                    Toast.makeText(this, "Student ID Card Not Yet Registered", Toast.LENGTH_SHORT).show();
                }

            }

        // Update attendance TextView with the scanned tag's data
           // updateAttendanceTextView(studentName);
            //updatePresentStudentsTextView();
           // attendanceTextView.setText("Attendance Marked: \n" + toHex(tag.getId()));
        }

//For detection
private String detectTagData(Tag tag) {
    StringBuilder sb = new StringBuilder();
    byte[] id = tag.getId();
    sb.append("ID (hex): ").append(toHex(id)).append('\n');
    sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
    sb.append("ID (dec): ").append(toDec(id)).append('\n');
    sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

    String prefix = "android.nfc.tech.";
    sb.append("Technologies: ");
    for (String tech : tag.getTechList()) {
        sb.append(tech.substring(prefix.length()));
        sb.append(", ");
    }

    sb.delete(sb.length() - 2, sb.length());

    for (String tech : tag.getTechList()) {

        if (tech.equals(MifareClassic.class.getName())) {
            sb.append('\n');
            String type = "Unknown";

            try {
                MifareClassic mifareTag = MifareClassic.get(tag);

                switch (mifareTag.getType()) {
                    case MifareClassic.TYPE_CLASSIC:
                        type = "Classic";
                        break;
                    case MifareClassic.TYPE_PLUS:
                        type = "Plus";
                        break;
                    case MifareClassic.TYPE_PRO:
                        type = "Pro";
                        break;
                }
                sb.append("Mifare Classic type: ");
                sb.append(type);
                sb.append('\n');

                sb.append("Mifare size: ");
                sb.append(mifareTag.getSize() + " bytes");
                sb.append('\n');

                sb.append("Mifare sectors: ");
                sb.append(mifareTag.getSectorCount());
                sb.append('\n');

                sb.append("Mifare blocks: ");
                sb.append(mifareTag.getBlockCount());
            } catch (Exception e) {
                sb.append("Mifare classic error: " + e.getMessage());
            }
        }

        if (tech.equals(MifareUltralight.class.getName())) {
            sb.append('\n');
            MifareUltralight mifareUlTag = MifareUltralight.get(tag);
            String type = "Unknown";
            switch (mifareUlTag.getType()) {
                case MifareUltralight.TYPE_ULTRALIGHT:
                    type = "Ultralight";
                    break;
                case MifareUltralight.TYPE_ULTRALIGHT_C:
                    type = "Ultralight C";
                    break;
            }
            sb.append("Mifare Ultralight type: ");
            sb.append(type);
        }
    }
    Log.v(TAG,sb.toString());
    return sb.toString();
}

    //For reading and writing
//    private String detectTagData(Tag tag) {
//        StringBuilder sb = new StringBuilder();
//        byte[] id = tag.getId();
//        sb.append("NFC ID (dec): ").append(toDec(id)).append('\n');
//        for (String tech : tag.getTechList()) {
//            if (tech.equals(MifareUltralight.class.getName())) {
//                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
//                String payload;
//                payload = readTag(mifareUlTag);
//                sb.append("payload: ");
//                sb.append(payload);
//                writeTag(mifareUlTag);
//            }
//        }
//    Log.v("test",sb.toString());
//    return sb.toString();
//}
    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = 0; i < bytes.length; ++i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffl;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }
    public void writeTag(MifareUltralight mifareUlTag) {
        try {
            mifareUlTag.connect();
            mifareUlTag.writePage(4, "get ".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(5, "fast".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(6, " NFC".getBytes(Charset.forName("US-ASCII")));
            mifareUlTag.writePage(7, " now".getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight...", e);
        } finally {
            try {
                mifareUlTag.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }
    public String readTag(MifareUltralight mifareUlTag) {
        try {
            mifareUlTag.connect();
            byte[] payload = mifareUlTag.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            Log.e(TAG, "IOException while reading MifareUltralight message...", e);
        } finally {
            if (mifareUlTag != null) {
                try {
                    mifareUlTag.close();
                }
                catch (IOException e) {
                    Log.e(TAG, "Error closing tag...", e);
                }
            }
        }
        return null;
    }
}



