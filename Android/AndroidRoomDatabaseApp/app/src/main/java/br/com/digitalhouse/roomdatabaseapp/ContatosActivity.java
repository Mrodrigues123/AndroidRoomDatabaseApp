package br.com.digitalhouse.roomdatabaseapp;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import br.com.digitalhouse.roomdatabaseapp.adapters.RecyclerViewContatosAdapter;
import br.com.digitalhouse.roomdatabaseapp.data.database.Database;
import br.com.digitalhouse.roomdatabaseapp.data.database.dao.ContatosDAO;
import br.com.digitalhouse.roomdatabaseapp.interfaces.RecyclerViewOnItemClickListener;
import br.com.digitalhouse.roomdatabaseapp.model.Contato;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ContatosActivity extends AppCompatActivity implements RecyclerViewOnItemClickListener {
    private FloatingActionButton fab;
    private RecyclerView recyclerViewContatos;
    private RecyclerViewContatosAdapter adapter;
    private List<Contato> contatoList = new ArrayList<>();
    private ContatosDAO dao;
    private TextInputLayout textInputLayoutNome;
    private TextInputLayout textInputLayoutTelefone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contatos);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = findViewById(R.id.fab);
        recyclerViewContatos = findViewById(R.id.recyclerViewContatos);
        textInputLayoutNome = findViewById(R.id.textInputLayoutNome);
        textInputLayoutTelefone = findViewById(R.id.textInputLayoutTelefone);

        // Criamos o layoutmanager para o RecycleView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //Setamos o layoutManager no recyclerView
        recyclerViewContatos.setLayoutManager(layoutManager);

        // Inicializamos o adapter passando a lista vazia e o listener de click do item
        adapter = new RecyclerViewContatosAdapter(contatoList, this);

        //setamos o adapter no recyclerView
        recyclerViewContatos.setAdapter(adapter);

        dao = Database.getDatabase(this).contatosDAO();

        fab.setOnClickListener(view -> {
            // Salvar item no banco ao clicar
            String nome = textInputLayoutNome.getEditText().getText().toString();
            String telefone = textInputLayoutTelefone.getEditText().getText().toString();

            new Thread(() -> {
                dao.insert(new Contato(nome, telefone));
                buscarTodosOsContatos();
            }).start();
        });

        // buscar todos os item salvos na base de dados e carregar no recyclerview
        buscarTodosOsContatos();
    }

    public void buscarTodosOsContatos(){

        // Uso de thread
        /*new Thread(() -> {
            List<Contato> contatos = dao.getAll();

            runOnUiThread(() -> {
                adapter.update(contatos);
            });

        }).start();*/

        dao.getAllRxJava()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(contatos -> {
                    adapter.update(contatos);
                }, throwable -> {
                    Log.i("TAG", "buscarTodosOsContatos: "+ throwable.getMessage());
                });
    }

    @Override
    public void onItemClick(Contato contato) {
        // ao clicar no item, deletar e remover da lista
        new Thread(() -> {
            dao.delete(contato);
            buscarTodosOsContatos();
        }).start();
    }
}
