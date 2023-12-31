package net.nullsum.freedoom;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OptionsFragment extends Fragment {
  String LOG = "OptionsFragment";

  TextView basePathTextView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View mainView = inflater.inflate(R.layout.fragment_options, null);
    Spinner resSpinnder = mainView.findViewById(R.id.resolution_div_spinner);

    List<String> list = new ArrayList<>();
    list.add("1");
    list.add("2");
    list.add("3");
    list.add("4");
    list.add("5");
    list.add("6");
    list.add("7");
    list.add("8");

    ArrayAdapter<String> dataAdapter =
        new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, list);
    dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    resSpinnder.setAdapter(dataAdapter);
    resSpinnder.setOnItemSelectedListener(
        new OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            AppSettings.setIntOption(getActivity(), "gzdoom_res_div", position + 1);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });
    int selected = AppSettings.getIntOption(getActivity(), "gzdoom_res_div", 1);
    resSpinnder.setSelection(selected - 1);

    basePathTextView = mainView.findViewById(R.id.base_path_textview);

    basePathTextView.setText(AppSettings.freedoomBaseDir);

    Button chooseDir = mainView.findViewById(R.id.choose_base_button);
    chooseDir.setOnClickListener(
        v -> {
          DirectoryChooserDialog directoryChooserDialog =
              new DirectoryChooserDialog(getActivity(), this::updateBaseDir);

          directoryChooserDialog.chooseDirectory(AppSettings.freedoomBaseDir);
        });

    Button resetDir = mainView.findViewById(R.id.reset_base_button);
    resetDir.setOnClickListener(
        v -> {
          AppSettings.resetBaseDir(getActivity());
          updateBaseDir(AppSettings.freedoomBaseDir);
        });

    Button sdcardDir = mainView.findViewById(R.id.sdcard_base_button);
    sdcardDir.setOnClickListener(
        v -> {
          File[] files = getActivity().getExternalFilesDirs(null);

          if ((files.length < 2) || (files[1] == null)) {
            showError("Can not find an external SD Card, is the card inserted?");
            return;
          }

          final String path = files[1].toString();

          Builder dialogBuilder = new Builder(getActivity());
          dialogBuilder.setTitle("WARNING");
          dialogBuilder.setMessage(
              "This will use the special location on the external SD Card which can be written to"
                  + " by this app, Android will DELETE this area when you uninstall the app and you"
                  + " will LOSE YOUR SAVEGAMES and game data!");
          dialogBuilder.setPositiveButton("OK", (dialog, which) -> updateBaseDir(path));
          dialogBuilder.setNegativeButton("Cancel", (dialog, which) -> {});

          final AlertDialog errdialog = dialogBuilder.create();
          errdialog.show();
        });

    return mainView;
  }

  private void updateBaseDir(String dir) {
    File fdir = new File(dir);

    if (!fdir.isDirectory()) {
      showError(dir + " is not a directory");
      return;
    }

    if (!fdir.canWrite()) {
      showError(dir + " is not a writable");
      return;
    }

    // Test CAN actually write, the above canWrite can pass on KitKat SD cards WTF GOOGLE
    File test_write = new File(dir, "test_write");
    try {
      test_write.createNewFile();
      if (!test_write.exists()) {
        showError(dir + " is not a writable");
        return;
      }
    } catch (IOException e) {
      showError(dir + " is not a writable");
      return;
    }
    test_write.delete();

    if (dir.contains(" ")) {
      showError(dir + " must not contain any spaces");
      return;
    }

    AppSettings.freedoomBaseDir = dir;
    AppSettings.setStringOption(getActivity(), "base_path", AppSettings.freedoomBaseDir);
    AppSettings.createDirectories(getActivity());

    basePathTextView.setText(AppSettings.freedoomBaseDir);
  }

  private void showError(String error) {
    AlertDialog.Builder dialogBuilder = new Builder(getActivity());
    dialogBuilder.setTitle(error);
    dialogBuilder.setPositiveButton("OK", (dialog, which) -> {});

    final AlertDialog errdialog = dialogBuilder.create();
    errdialog.show();
  }
}
