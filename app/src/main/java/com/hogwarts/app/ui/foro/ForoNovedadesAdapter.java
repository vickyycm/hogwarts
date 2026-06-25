package com.hogwarts.app.ui.foro;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hogwarts.app.R;
import com.hogwarts.app.model.Novedad;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ForoNovedadesAdapter extends RecyclerView.Adapter<ForoNovedadesAdapter.NovedadViewHolder> {

    private List<Novedad> listaNovedades;

    public ForoNovedadesAdapter(List<Novedad> listaNovedades) {
        this.listaNovedades = listaNovedades;
    }

    @NonNull
    @Override
    public NovedadViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_novedad, parent, false);
        return new NovedadViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NovedadViewHolder holder, int position) {
        Novedad novedad = listaNovedades.get(position);
        holder.bind(novedad);
    }

    @Override
    public int getItemCount() {
        return listaNovedades != null ? listaNovedades.size() : 0;
    }

    public void actualizarLista(List<Novedad> nuevaLista) {
        this.listaNovedades = nuevaLista;
        notifyDataSetChanged();
    }

    static class NovedadViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvFecha, tvTitulo, tvContenido;

        public NovedadViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFecha = itemView.findViewById(R.id.tvFechaNovedad);
            tvTitulo = itemView.findViewById(R.id.tvTituloNovedad);
            tvContenido = itemView.findViewById(R.id.tvContenidoNovedad);
        }

        public void bind(Novedad novedad) {
            tvTitulo.setText(novedad.getTitulo());
            tvContenido.setText(novedad.getContenido());

            if (novedad.getFecha() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                String fechaFormateada = sdf.format(novedad.getFecha().toDate());
                tvFecha.setText(fechaFormateada);
            } else {
                tvFecha.setText("");
            }
        }
    }
}