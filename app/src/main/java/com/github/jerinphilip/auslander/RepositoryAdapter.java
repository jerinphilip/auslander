package com.github.jerinphilip.auslander;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class RepositoryAdapter extends ArrayAdapter<Repository> {

  public RepositoryAdapter(Context context, ArrayList<Repository> repositories) {
    super(context, 0, repositories);
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    Repository repository = getItem(position);

    if (convertView == null) {
      convertView =
          LayoutInflater.from(getContext()).inflate(R.layout.repository_list_item, parent, false);
    }

    TextView nameTextView = convertView.findViewById(R.id.text_name);
    TextView urlTextView = convertView.findViewById(R.id.text_url);

    nameTextView.setText(repository.getName());
    urlTextView.setText(repository.getUrl());

    return convertView;
  }

  public void update(ArrayList<Repository> repositories) {
    clear(); // Clear existing data
    addAll(repositories); // Add new data
    notifyDataSetChanged(); // Notify adapter of data change
  }
}
