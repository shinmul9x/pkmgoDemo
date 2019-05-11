package com.example.pokemongo1;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class PokemonAdapter extends RecyclerView.Adapter<PokemonAdapter.ViewHolder>{
    ArrayList<Pokemonp> pokemons;
    Context context1,context2;


    public PokemonAdapter(ArrayList<Pokemonp> pokemons, Context context1, Context context2) {
        this.pokemons = pokemons;
        this.context1 = context1;
        this.context2 = context2;
    }

    public PokemonAdapter(ArrayList<Pokemonp> pokemonps) {
        this.pokemons = pokemonps;
    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.item_row,parent,false);

        ViewHolder viewHolder = new ViewHolder(itemView);

        //pokemons là cái arraylist của adapter, class của nó có getName sao
        // nó không lấy đc nhỉ
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( ViewHolder viewHolder, int i) {
        viewHolder.txtName.setText(pokemons.get(i).getName());
        viewHolder.txtPower.setText(pokemons.get(i).getPower());
        viewHolder.imgHinh.setImageResource(pokemons.get(i).getImage());





    }

    @Override
    public int getItemCount() {
        return pokemons.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        RelativeLayout itemzero;
        TextView txtName, txtPower;
        ImageView imgHinh;
        public ViewHolder( View itemView) {
            super(itemView);
            itemzero =(RelativeLayout) itemView.findViewById(R.id.itemzero);
            txtName = (TextView)itemView.findViewById(R.id.txtName);
            txtPower =(TextView) itemView.findViewById(R.id.txtPower);
            imgHinh =(ImageView) itemView.findViewById(R.id.imgHinh);
        }
    }

}
