package com.hogwarts.app.ui.hechizos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Hechizo;
import java.util.List;

public class ListaHechizosAdapter extends RecyclerView.Adapter<ListaHechizosAdapter.HechizoViewHolder> {

    private List<Hechizo> listaHechizos;
    private OnHechizoClickListener listener;

    public interface OnHechizoClickListener {
        void onEliminarClick(int position, String hechizoId);
        void onEditarClick(Hechizo hechizo);
        void onItemClick(Hechizo hechizo);
    }

    public ListaHechizosAdapter(List<Hechizo> listaHechizos, OnHechizoClickListener listener) {
        this.listaHechizos = listaHechizos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HechizoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HechizoViewHolder(
                LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_hechizo, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull HechizoViewHolder holder, int position) {
        Hechizo hechizo = listaHechizos.get(position);
        holder.bind(hechizo, position);
    }

    @Override
    public int getItemCount() {
        return listaHechizos != null ? listaHechizos.size() : 0;
    }

    public void actualizarLista(List<Hechizo> nuevaLista) {
        this.listaHechizos = nuevaLista;
        notifyDataSetChanged();
    }

    public class HechizoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvNombre, tvDesc;
        private ImageButton btnEditar, btnEliminar;

        public HechizoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvNombreHechizo);
            tvDesc = itemView.findViewById(R.id.tvDescHechizo);
            btnEditar = itemView.findViewById(R.id.btnEditarHechizo);
            btnEliminar = itemView.findViewById(R.id.btnEliminarHechizo);
        }

        public void bind(Hechizo hechizo, int position) {
            tvNombre.setText(hechizo.getAnotaciones());

            if (hechizo.getAnotaciones() != null) {
                tvDesc.setText(hechizo.getAnotaciones());
            } else {
                tvDesc.setText("");
            }

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEliminarClick(position, hechizo.getId());
                }
            });

            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditarClick(hechizo);
                }
            });

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemClick(hechizo);
                }
            });
        }
    }
}