package pconfiguracaofirebase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ConfiguracoesFirebase {
    private static DatabaseReference database;
    private static FirebaseAuth auth;

    //metodo retornar instancia DATABASE
    public static DatabaseReference metodoPegarFirebaseDataBase() {
        if (database == null) {
            database = FirebaseDatabase.getInstance().getReference();
        }
        //se tiver nao precisa criar novamente
        return database;
    }

    //metodo retornar instancia FIREBASEAUTH
    public static FirebaseAuth metodopegarAutenticacaoFirebaseAuth() {
        if (auth == null) {
            auth = FirebaseAuth.getInstance();
        }
        //se tiver nao precisa criar novamente
        return auth;
    }
}
