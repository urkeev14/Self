package adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import model.Journal;
import project.fragments.self.R;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {

    private Context context;
    private List<Journal> journals;

    public JournalRecyclerAdapter(Context context, List<Journal> journals) {
        this.context = context;
        this.journals = journals;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.journal_row, parent, false);

        return new ViewHolder(view, context);
    }

    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        Journal journal = journals.get(position);

        holder.tvJournalTitleList.setText(journal.getTitle());
        holder.tvJournalThoughtList.setText(journal.getThought());
        holder.tvJournalRowUsername.setText(journal.getUserName());
        Picasso.get()
                .load(journal.getImageUrl())
                .placeholder(R.drawable.image_three)
                .fit()
                .into(holder.ivJournalImageList);

        String timeAgo = (String) DateUtils
                .getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);

        holder.tvJournalTimestamp.setText(timeAgo);


    }

    @Override
    public int getItemCount() {
        return journals.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public ImageView ivJournalImageList;
        public ImageButton ibtnShare;
        public TextView tvJournalTitleList;
        public TextView tvJournalThoughtList;
        public TextView tvJournalTimestamp;
        public TextView tvJournalRowUsername;
        public String username;
        public String userId;

        public ViewHolder(View itemView, Context ctx) {
            super(itemView);
            context = ctx;

            initComponents();
        }

        private void initComponents() {
            ivJournalImageList = itemView.findViewById(R.id.ivJournalImageList);
            ibtnShare = itemView.findViewById(R.id.ibtnShare);
            ibtnShare.setOnClickListener(this);
            tvJournalTitleList = itemView.findViewById(R.id.tvJournalTitleList);
            tvJournalThoughtList = itemView.findViewById(R.id.tvJournalThoughtList);
            tvJournalTimestamp = itemView.findViewById(R.id.tvJournalTimestamp);
            tvJournalRowUsername = itemView.findViewById(R.id.tvJournalRowUsername);

        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.ibtnShare:
                    shareJournal();
                    break;
                default:
                    break;

            }
        }

        private void shareJournal() {
            Toast.makeText(context, "Share button clicked !", Toast.LENGTH_SHORT).show();
            //context.startActivity();
        }
    }
}
