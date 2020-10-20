package com.bobo.vigilancetimer.ui.main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bobo.vigilancetimer.DBUtils;
import com.bobo.vigilancetimer.R;

//import com.bobo.test.DBActions;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private PageViewModel pageViewModel;

    public static PlaceholderFragment newInstance(int index) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_main, container, false);

        final DBUtils dbUtils = new DBUtils();
        // Initiate widget
        final Button btn_startTrial = (Button) root.findViewById(R.id.startTrial);
        final Button btn_startVigilance = (Button) root.findViewById(R.id.startVigilance);
        final Button btn_startPhone = (Button) root.findViewById(R.id.startPhone);
        final Button btn_miscInfo = (Button) root.findViewById(R.id.miscInfo);
        final RadioGroup rg_focusDirection = (RadioGroup) root.findViewById(R.id.focusDirection);

        //Init button state
        btn_startVigilance.setEnabled(false);
        btn_startPhone.setEnabled(false);
        btn_miscInfo.setEnabled(false);

        btn_startTrial.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start a new trial
                if(btn_startTrial.getText() == getResources().getString(R.string.startTrial)){
                    // Init a new record for each trial
                    // Variable operation
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dbUtils.trialStartInMs = System.currentTimeMillis();
                            dbUtils.initTrial();
                        }
                    }).start();
                    // UI modification
                    btn_startVigilance.setEnabled(true);
                    btn_startPhone.setEnabled(true);
                    btn_miscInfo.setEnabled(true);
                    btn_startTrial.setText(getResources().getString(R.string.endTrial));
                }
                // End a trial, stop every record with current time (if needed)
                else if(btn_startTrial.getText() == getResources().getString(R.string.endTrial)){
                    // Variable operation
                    AlertDialog.Builder builder = new AlertDialog.Builder(root.getContext());
                    builder.setTitle(getResources().getString(R.string.uploadPrompt));
                    builder.setMessage(getResources().getString(R.string.uploadConfirm));
                    builder.setPositiveButton(getResources().getString(R.string.doUpload), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //DB IO
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dbUtils.trialEndInMs = System.currentTimeMillis();
                                    dbUtils.insertTrialInfo();
                                }
                            }).start();
                            btn_startTrial.setText(getResources().getString(R.string.startTrial));
                            btn_startVigilance.setEnabled(false);
                            btn_startPhone.setEnabled(false);
                            btn_miscInfo.setEnabled(false);
                        }
                    });
                    builder.setNegativeButton(getResources().getString(R.string.notUplaod), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    dbUtils.reset();
                                }
                            }).start();
                            btn_startTrial.setText(getResources().getString(R.string.startTrial));
                            btn_startVigilance.setEnabled(false);
                            btn_startPhone.setEnabled(false);
                            btn_miscInfo.setEnabled(false);
                        }
                    });
                    builder.setNeutralButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Do nothing.
                        }
                    });
                    builder.show();
                }

            }
        });

        btn_startVigilance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_startVigilance.getText() == getResources().getString(R.string.startVigilance)){
                    dbUtils.vigilanceStartInMs = System.currentTimeMillis();
                    // UI modification
                    rg_focusDirection.clearCheck();
                    btn_startVigilance.setText(getResources().getString(R.string.endVigilance));
                    btn_startTrial.setEnabled(false);
                }
                else if(btn_startVigilance.getText() == getResources().getString(R.string.endVigilance)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dbUtils.vigilanceEndInMs = System.currentTimeMillis();
                            dbUtils.insertVigilanceInfo();
                        }
                    }).start();
                    // UI modification
                    btn_startVigilance.setText(getResources().getString(R.string.startVigilance));
                    btn_startTrial.setEnabled(true);
                }
            }
        });

        btn_miscInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                miscInfoDialog(root,dbUtils);
            }
        });

        rg_focusDirection.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.focusOpposite:
                        dbUtils.focusDirection = "O";
                        break;
                    case R.id.focusSide:
                        dbUtils.focusDirection = "S";
                        break;
                    case R.id.focusBack:
                        dbUtils.focusDirection = "B";
                        break;
                    default:
                        dbUtils.focusDirection = "-";
                        break;
                }
            }
        });


        btn_startPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btn_startPhone.getText() == getResources().getString(R.string.startPhone)){
                    dbUtils.phoneStartInMs = System.currentTimeMillis();
                    btn_startPhone.setText(getResources().getString(R.string.endPhone));
                    btn_startTrial.setEnabled(false);
                }
                else if(btn_startPhone.getText() == getResources().getString(R.string.endPhone)){
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            dbUtils.phoneEndInMs = System.currentTimeMillis();
                            dbUtils.insertPhoneInfo();
                        }
                    }).start();
                    btn_startPhone.setText(getResources().getString(R.string.startPhone));
                    btn_startTrial.setEnabled(true);
                }
            }
        });

        return root;
    }



    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    private void miscInfoDialog(View view, final DBUtils dbUtils){
        final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        final AlertDialog dialog = builder.create();
        final View dialogView = View.inflate(view.getContext(), R.layout.miscinfo, null);
        dialog.setView(dialogView);
        dialog.show();
        final RadioGroup rg_trialGender = (RadioGroup) dialogView.findViewById(R.id.trialGender);
        final RadioGroup rg_isFullRec = (RadioGroup) dialogView.findViewById(R.id.isFullRec);
        final RadioGroup rg_isEarphone = (RadioGroup) dialogView.findViewById(R.id.isUseEarphone);
        final EditText et_groupSize = (EditText) dialogView.findViewById(R.id.groupSize);
        final EditText et_foodType = (EditText) dialogView.findViewById(R.id.foodType);
        final EditText et_extraInfo = (EditText) dialogView.findViewById(R.id.extraInfo);
        final Button btn_save = (Button) dialogView.findViewById(R.id.saveMiscInfo);
        rg_trialGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.focusMale:
                        dbUtils.trialGender = "M";
                        break;
                    case R.id.focusFemale:
                        dbUtils.trialGender = "F";
                        break;
                    default:
                        dbUtils.trialGender = "-";
                        break;
                }
            }
        });

        rg_isFullRec.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.isFull:
                        dbUtils.isFullRec = true;
                        break;
                    case R.id.notFull:
                        dbUtils.isFullRec = false;
                        break;
                    default:
                        dbUtils.isFullRec = false;
                        break;
                }
            }
        });

        rg_isEarphone.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i){
                    case R.id.isUse:
                        dbUtils.isUseEarphone = true;
                        break;
                    case R.id.notUse:
                        dbUtils.isUseEarphone = false;
                        break;
                    default:
                        dbUtils.isUseEarphone = false;
                        break;
                }
            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sizeNumber = et_groupSize.getText().toString();
                try {
                    dbUtils.groupSize = Integer.parseInt(sizeNumber);
                    dbUtils.foodType = et_foodType.getText().toString();
                    dbUtils.extraInfo = et_extraInfo.getText().toString();
                } catch (Exception e){
                    Log.e("MiscInfo",Log.getStackTraceString(e));
                }
                dialog.dismiss();
            }
        });
    }
}