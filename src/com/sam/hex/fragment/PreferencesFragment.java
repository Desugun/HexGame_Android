package com.sam.hex.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;

import com.sam.hex.R;
import com.sam.hex.Settings;

/**
 * @author Will Harmon
 **/
@SuppressLint("NewApi")
public class PreferencesFragment extends PreferenceFragment {
    SharedPreferences settings;
    Preference gridPref;
    Preference timerPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings = PreferenceManager.getDefaultSharedPreferences(getActivity());
        loadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        setListeners();
    }

    private class DifficultyListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            preference.setSummary(getResources().getStringArray(R.array.comDifficultyArray)[Integer.valueOf(newValue.toString())]);
            return true;
        }
    }

    private class GridListener implements OnPreferenceChangeListener {
        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            if(newValue.toString().equals("0")) {
                // Custom value needed
                showInputDialog(getString(R.string.preferences_summary_custom_game_size));
                return false;
            }
            else {
                preference.setSummary(String.format(getString(R.string.preferences_summary_game_size), newValue, newValue));
                return true;
            }
        }
    }

    private class TimerListener implements OnPreferenceClickListener {
        @Override
        public boolean onPreferenceClick(Preference pref) {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialoglayout = inflater.inflate(R.layout.preferences_timer, null);
            final Spinner timerType = (Spinner) dialoglayout.findViewById(R.id.timerType);
            final EditText timer = (EditText) dialoglayout.findViewById(R.id.timer);
            timer.setText(settings.getString("timerPref", getString(R.integer.DEFAULT_TIMER_TIME)));
            timerType.setSelection(Integer.valueOf(settings.getString("timerTypePref", getString(R.integer.DEFAULT_TIMER_TYPE))));
            timerType.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int arg2, long arg3) {
                    if(arg2 > 0) {
                        timer.setVisibility(View.VISIBLE);
                    }
                    else {
                        timer.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {
                    timer.setVisibility(View.GONE);
                }
            });
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setView(dialoglayout).setPositiveButton(getString(R.string.okay), new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String timerTime = timer.getText().toString();
                    if(timerTime.isEmpty()) timerTime = "0";
                    settings.edit().putString("timerTypePref", getResources().getStringArray(R.array.timerTypeValues)[timerType.getSelectedItemPosition()])
                            .commit();
                    settings.edit().putString("timerPref", timerTime).commit();
                }
            }).setNegativeButton(getString(R.string.cancel), null).show();
            return true;
        }
    }

    private void setListeners() {
        // Allow for custom grid sizes
        gridPref = findPreference("gameSizePref");
        if(gridPref != null) {
            String boardSize = String.valueOf(Settings.getGridSize(getActivity()));
            gridPref.setSummary(String.format(getString(R.string.preferences_summary_game_size), boardSize, boardSize));
            gridPref.setOnPreferenceChangeListener(new GridListener());
        }

        // Give a custom popup for timers
        timerPref = findPreference("timerOptionsPref");
        if(timerPref != null) {
            timerPref.setOnPreferenceClickListener(new TimerListener());
        }

        Preference comDifficultyPref = findPreference("comDifficulty");
        if(comDifficultyPref != null) {
            comDifficultyPref.setOnPreferenceChangeListener(new DifficultyListener());
            comDifficultyPref.setSummary(getResources().getStringArray(R.array.comDifficultyArray)[Settings.getComputerDifficulty(getActivity())]);
        }
    }

    private void loadPreferences() {
        addPreferencesFromResource(R.layout.preferences_general);
    }

    /**
     * Popup for custom grid sizes
     * */
    private void showInputDialog(String message) {
        final EditText editText = new EditText(getActivity());
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(message).setView(editText).setPositiveButton(getString(R.string.okay), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(!editText.getText().toString().equals("")) {
                    int input = Integer.decode(editText.getText().toString());
                    if(input > 30) {
                        input = 30;
                    }
                    else if(input < 4) {
                        input = 4;
                    }
                    settings.edit().putString("customGameSizePref", String.valueOf(input)).commit();
                    settings.edit().putString("gameSizePref", String.valueOf(0)).commit();
                    String boardSize = settings.getString("customGameSizePref", getString(R.integer.DEFAULT_BOARD_SIZE));
                    gridPref.setSummary(String.format(getString(R.string.preferences_summary_game_size), boardSize, boardSize));
                }
            }
        }).setNegativeButton(getString(R.string.cancel), null).show();
    }
}
