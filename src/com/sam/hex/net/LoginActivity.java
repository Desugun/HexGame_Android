package com.sam.hex.net;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.sam.hex.DialogBox;
import com.sam.hex.GameAction;
import com.sam.hex.R;
import com.sam.hex.activity.DefaultActivity;
import com.sam.hex.net.igGC.ParsedDataset;
import com.sam.hex.net.igGC.igGameCenter;

/**
 * @author Will Harmon
 **/
public class LoginActivity extends DefaultActivity {
    SharedPreferences settings;
    Context context;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        context = getApplicationContext();

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        Button enter = (Button) findViewById(R.id.loginEnter);
        final EditText username = (EditText) findViewById(R.id.username);
        username.setText(settings.getString("netUsername", ""));
        final EditText password = (EditText) findViewById(R.id.password);
        enter.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String encryptedPassword = GameAction.md5(password.getText().toString());
                            ParsedDataset parsedDataset = igGameCenter.login(username.getText().toString(), encryptedPassword, "");
                            if(!parsedDataset.error) {
                                settings.edit().putString("netUsername", username.getText().toString()).commit();
                                settings.edit().putString("netPassword", encryptedPassword).commit();

                                startActivity(new Intent(getBaseContext(), NetLobbyActivity.class));
                                finish();
                            }
                            else {
                                System.out.println(parsedDataset.getErrorMessage());
                                new DialogBox(LoginActivity.this, context.getString(R.string.loginFailed), new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch(which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            // Yes button clicked
                                            startActivity(new Intent(getBaseContext(), RegistrationActivity.class));
                                            break;
                                        case DialogInterface.BUTTON_NEGATIVE:
                                            // No button clicked
                                            break;
                                        }
                                    }
                                }, context.getString(R.string.register), context.getString(R.string.cancel));
                            }
                        }
                        catch(MalformedURLException e) {
                            e.printStackTrace();
                        }
                        catch(ParserConfigurationException e) {
                            e.printStackTrace();
                        }
                        catch(SAXException e) {
                            e.printStackTrace();
                        }
                        catch(IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });

        Button register = (Button) findViewById(R.id.registerEnter);
        register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getBaseContext(), RegistrationActivity.class));
                finish();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        if(!isOnline()) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch(which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        // Yes button clicked
                        startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        // No button clicked
                        finish();
                        break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getApplicationContext().getString(R.string.cantConnect))
                    .setPositiveButton(getApplicationContext().getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(getApplicationContext().getString(R.string.no), dialogClickListener).setCancelable(false).show();
        }
    }

    private boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        boolean connected = false;
        try {
            connected = cm.getActiveNetworkInfo().isConnected();
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
        return connected;
    }
}
