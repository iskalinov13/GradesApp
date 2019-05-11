package com.example.gradesapp;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AdapterTestCard extends RecyclerView.Adapter<AdapterTestCard.TestCardViewHolder> {
    List<TestCard> activeTestCards;
    List<TestCard> testCardsCreator;
    View.OnClickListener onClickListener;
    View.OnLongClickListener onLongClickListener;
    boolean FilterCard = false;

    public AdapterTestCard(List<TestCard> activeTestCards, List<TestCard> testCardsCreator) {
        this.activeTestCards = activeTestCards;
        this.testCardsCreator = testCardsCreator;
    }

    @Override
    public TestCardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        view.setOnClickListener(onClickListener);
        view.setOnLongClickListener(onLongClickListener);
        TestCardViewHolder TestCardViewHolder = new TestCardViewHolder(view);
        return TestCardViewHolder;
    }

    public boolean isFilterCard() {
        return FilterCard;
    }

    public void setFilterCard(boolean FilterCard) {
        this.FilterCard = FilterCard;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(TestCardViewHolder holder, int position) {
        TestCard TestCard;
        if (FilterCard) {
            TestCard = testCardsCreator.get(position);
        } else {
            TestCard = activeTestCards.get(position);
        }
        if (TestCard.isActive()) {
            holder.textViewActive.setText("Yes");
        } else {
            holder.textViewActive.setText("No");
        }
        holder.textViewTitle.setText(TestCard.getTitle());
        holder.textViewDescripcion.setText(TestCard.getDescription());
        holder.textViewCreator.setText(TestCard.getCreator());
        holder.textViewNumberOfQuestions.setText("" + TestCard.getNumberOfQuestions());
        String ts = getDate(TestCard.getCreationDate());
        holder.textViewCreationDate.setText(ts);
        if (position == 0) {
            holder.list_item_parent.setBackgroundResource(R.drawable.first);
        }
        else if (position % 2 == 1) {
            holder.list_item_parent.setBackgroundResource(R.drawable.second);
        }
        else if (position % 2 == 0) {
            holder.list_item_parent.setBackgroundResource(R.drawable.third);
        }
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy", cal).toString();
        return date;
    }

    @Override
    public int getItemCount() {
        if (FilterCard) {
            if (testCardsCreator == null) return 0;
            else return testCardsCreator.size();
        } else {
            if (activeTestCards == null) return 0;
            else return activeTestCards.size();
        }
    }

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public static class TestCardViewHolder extends RecyclerView.ViewHolder {
        TextView textViewActive;
        TextView textViewTitle;
        TextView textViewDescripcion;
        TextView textViewCreator;
        TextView textViewNumberOfQuestions;
        TextView textViewCreationDate;
        ConstraintLayout list_item_parent;

        public TestCardViewHolder(View itemView) {
            super(itemView);
            list_item_parent  =(ConstraintLayout)itemView.findViewById(R.id.content_main);
            textViewActive = (TextView) itemView.findViewById(R.id.textViewActive);
            textViewTitle = (TextView) itemView.findViewById(R.id.textViewTitle);
            textViewDescripcion = (TextView) itemView.findViewById(R.id.textViewDescription);
            textViewNumberOfQuestions = (TextView) itemView.findViewById(R.id.textViewNumberOfQuestions);
            textViewCreator = (TextView) itemView.findViewById(R.id.textViewCreate);
            textViewCreationDate = (TextView) itemView.findViewById(R.id.textViewCreationDate);
        }
    }
}
