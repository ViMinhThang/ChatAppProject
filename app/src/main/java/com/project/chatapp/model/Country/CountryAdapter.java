package com.project.chatapp.model.Country;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.chatapp.R;

import java.util.ArrayList;
import java.util.List;

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.CountryViewHolder> implements Filterable {

    private List<Country> countryList;
    private List<Country> countryListFull;

    public CountryAdapter(List<Country> countryList) {
        this.countryList = countryList;
        countryListFull = new ArrayList<>(countryList);
    }

    @NonNull
    @Override
    public CountryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_country, parent, false);
        return new CountryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull CountryViewHolder holder, int position) {
        Country currentCountry = countryList.get(position);
        holder.imageViewFlag.setImageResource(currentCountry.getFlagResId());
        holder.textViewName.setText(currentCountry.getName());
        holder.textViewCode.setText(currentCountry.getCode());
    }

    @Override
    public int getItemCount() {
        return countryList.size();
    }

    @Override
    public Filter getFilter() {
        return countryFilter;
    }

    private Filter countryFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Country> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(countryListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Country country : countryListFull) {
                    if (country.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(country);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            countryList.clear();
            countryList.addAll((List) results.values);
            notifyDataSetChanged();
        }
    };

    class CountryViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewFlag;
        TextView textViewName;
        TextView textViewCode;

        CountryViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewFlag = itemView.findViewById(R.id.imageView_flag);
            textViewName = itemView.findViewById(R.id.textView_name);
            textViewCode = itemView.findViewById(R.id.textView_code);
        }
    }
}