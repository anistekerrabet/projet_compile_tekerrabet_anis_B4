import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class AnalyseurComplet {
    
    // ==================== VARIABLES GLOBALES ====================
    
    // Variables pour l'analyseur lexical
    static boolean analyseLexicaleReussie = true;
    static String[] operateur = {
        "+", "-", "*", "/", "%", "++", "--", 
        "==", "!=", "<", ">", "<=", ">=", 
        "&&", "||", "!", "&", "^", "~", 
        "<<", ">>", "=", "+=", "-=", "*=", 
        "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", 
        "->", ".", "[", "]", "(", ")", "{", "}", 
        ";", ",", "?", ":", "/*", "*/", "#"
    };
    
    static int[][] Mat = {
        {1, -1, 1, -1}, // état 0 (initial)
        {1, 1, 1, -1}   // état 1 (acceptant - identifiant valide)
    };
    
    static int Ef = 1; // État final
    
    // Variables pour l'analyseur syntaxique
    static boolean analyseSyntaxiqueReussie = true;
    static ArrayList<Character> tokens = new ArrayList<>();
    static int position = 0;
    static char currentToken;
    
    // ==================== ANALYSEUR LEXICAL ====================
    
    /**
     * Vérifie si une chaîne est un mot-clé du langage C
     */
    public static boolean existeMotCle(String ch) {
        String[] motsCles = {
            "int", "float", "double", "char", "void", 
            "if", "else", "while", "for", "do", 
            "switch", "case", "default", "break", "continue", 
            "return", "sizeof", "struct", "union", "enum", 
            "typedef", "extern", "static", "auto", "register", 
            "const", "volatile", "goto", "signed", "unsigned", 
            "short", "long", "include", "printf", "scanf", 
            "stdio.h", "main","TEKERRABET","ANIS"
        };
        
        for (String motCle : motsCles) {
            if (ch.equals(motCle)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Traite les mots-clés et écrit leur code dans le fichier
     */
    public static void traiterMotCle(String ch, FileWriter fw) throws IOException {
        String[] types = {"int", "float", "char", "void", "const", "double"};
        String[] instructionsNonTraitees = {"if", "else", "for", "do", "switch", "case"};
        
        // Vérifier les types de données
        for (String type : types) {
            if (ch.equals(type)) {
                fw.write("T");
                return;
            }
        }
        
        // Vérifier les instructions non traitées
        for (String instruction : instructionsNonTraitees) {
            if (ch.equals(instruction)) {
                System.err.println("Instruction non traitee : " + ch);
                analyseLexicaleReussie = false;
                return;
            }
        }
        
        // Traiter les mots-clés spécifiques
        switch (ch) {
            case "while":
                fw.write("W");
                break;
            case "stdio.h":
                fw.write("B");
                break;
            case "include":
                fw.write("C");
                break;
            case "main":
                fw.write("M");
                break;
            case "printf":
            case "scanf":
                fw.write("F");
                break;
            case "return":
                fw.write("R");
                break;
            default:
                fw.write("K");
                break;
        }
    }
    
    /**
     * Vérifie si un mot est un opérateur et l'écrit dans le fichier
     */
    public static boolean existeOperateur(String mot, FileWriter fw) throws IOException {
        for (String op : operateur) {
            if (mot.equals(op)) {
                fw.write(mot);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Retourne l'indice de colonne dans la matrice de transition
     */
    public static int col(char t) {
        if (Character.isLetter(t)) return 0;
        if (Character.isDigit(t)) return 1;
        if (t == '_') return 2;
        return 3;
    }
    
    /**
     * Vérifie si une chaîne est un nombre entier
     */
    public static boolean estNombre(String mot) {
        if (mot.isEmpty()) return false;
        
        for (int i = 0; i < mot.length(); i++) {
            if (!Character.isDigit(mot.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Vérifie si une chaîne est un nombre décimal
     */
    public static boolean estNombreDecimal(String mot) {
        if (mot.isEmpty()) return false;
        
        boolean pointVu = false;
        for (int i = 0; i < mot.length(); i++) {
            char c = mot.charAt(i);
            if (c == '.') {
                if (pointVu) return false;
                pointVu = true;
            } else if (!Character.isDigit(c)) {
                return false;
            }
        }
        return pointVu;
    }
    
    /**
     * Effectue l'analyse lexicale d'un fichier source
     */
    public static boolean analyseLexicale(String inPath, String outPath) {
        System.out.println("========================================================");
        System.out.println("         PHASE 1 : ANALYSE LEXICALE                    ");
        System.out.println("========================================================\n");
        
        System.out.println("Lecture du fichier : " + inPath);
        
        try (FileReader fr = new FileReader(inPath);
             FileWriter fw = new FileWriter(outPath)) {

            int c = fr.read();

            while (c != -1) {
                String mot = "";
                int Ec = 0;
                
                // Ignorer les espaces blancs
                while (c != -1 && Character.isWhitespace((char)c)) {
                    c = fr.read();
                }
                
                // Construire le mot selon l'automate
                while (c != -1 && !Character.isWhitespace((char)c) && Mat[Ec][col((char)c)] != -1) {
                    Ec = Mat[Ec][col((char)c)];
                    mot += (char)c;
                    c = fr.read();
                }
                
                // Si l'automate rejette mais il reste des caractères
                if (c != -1 && !Character.isWhitespace((char)c) && Mat[Ec][col((char)c)] == -1) {
                    while (c != -1 && !Character.isWhitespace((char)c)) {
                        mot += (char)c;
                        c = fr.read();
                    }
                }
                
                // Analyser le mot construit
                if (!mot.isEmpty()) {
                    if (existeMotCle(mot)) {
                        traiterMotCle(mot, fw);
                    } else if (existeOperateur(mot, fw)) {
                        // Opérateur déjà écrit
                    } else if (Ec == Ef) {
                        fw.write("I");
                    } else if (estNombreDecimal(mot)) {
                        fw.write("D");
                    } else if (estNombre(mot)) {
                        fw.write("N");
                    } else {
                        System.err.println("Lexeme invalide : " + mot);
                        analyseLexicaleReussie = false;
                    }
                }
            }

            System.out.println("Analyse lexicale terminee");
            System.out.println("Tokens ecrits dans : " + outPath + "\n");
            
            if (analyseLexicaleReussie) {
                System.out.println("Aucune erreur lexicale detectee\n");
            } else {
                System.out.println("Des erreurs lexicales ont ete detectees\n");
            }

        } catch (IOException e) {
            System.err.println("Erreur d'E/S : " + e.getMessage());
            return false;
        }
        
        return analyseLexicaleReussie;
    }
    
    // ==================== ANALYSEUR SYNTAXIQUE ====================
    
    /**
     * Lit tous les tokens depuis le fichier de sortie de l'analyseur lexical
     */
    public static void lireTokens(String fichier) throws IOException {
        tokens.clear();
        position = 0;
        
        try (FileReader fr = new FileReader(fichier)) {
            int c;
            while ((c = fr.read()) != -1) {
                char token = (char) c;
                if (!Character.isWhitespace(token)) {
                    tokens.add(token);
                }
            }
        }
        
        // Initialiser currentToken avec le premier token
        if (!tokens.isEmpty()) {
            currentToken = tokens.get(0);
        } else {
            currentToken = '\0';
        }
    }
    
    /**
     * Avance au token suivant
     */
    public static void avancer() {
        position++;
        if (position < tokens.size()) {
            currentToken = tokens.get(position);
        } else {
            currentToken = '\0';
        }
    }
    
    /**
     * Vérifie si le token courant correspond au token attendu et le consomme
     */
    public static boolean consommer(char tokenAttendu) {
        if (currentToken == tokenAttendu) {
            avancer();
            return true;
        } else {
            System.err.println("Erreur a la position " + position + 
                             " : attendu '" + tokenAttendu + "' mais trouve '" + currentToken + "'");
            analyseSyntaxiqueReussie = false;
            return false;
        }
    }
    
    /**
     * Vérifie si le token courant est dans l'ensemble donné
     */
    public static boolean estDans(char token, char... ensemble) {
        for (char c : ensemble) {
            if (token == c) {
                return true;
            }
        }
        return false;
    }
    
    // ==================== RÈGLES DE LA GRAMMAIRE ====================
    
    /**
     * Programme → #include<Directive> ListeFonctions MAIN
     */
    public static void Programme() {
        // #include<Directive>
        if (!consommer('#')) return;
        if (!consommer('C')) return;
        if (!consommer('<')) return;
        
        Directive();
        
        if (!consommer('>')) return;
        
        // ListeFonctions
        ListeFonctions();
        
        // MAIN
        MAIN();
        
        if (currentToken == '\0') {
            System.out.println("Fin du programme atteinte avec succes");
        } else {
            System.err.println("Erreur : tokens restants apres la fin du programme");
            analyseSyntaxiqueReussie = false;
        }
    }
    
    /**
     * Directive → stdio.h | stdlib.h | string.h
     */
    public static void Directive() {
        if (!consommer('B')) return;
    }
    
    /**
     * ListeFonctions → Fonction ListeFonctions | ε
     */
    public static void ListeFonctions() {
        if (currentToken == 'T') {
            int savePos = position;
            avancer();
            
            if (currentToken != 'M') {
                position = savePos;
                currentToken = tokens.get(position);
                
                Fonction();
                ListeFonctions();
            } else {
                position = savePos;
                currentToken = tokens.get(position);
            }
        }
    }
    
    /**
     * Fonction → Type ID ( Parametres ) { Corps }
     */
    public static void Fonction() {
        Type();
        if (!consommer('I')) return;
        if (!consommer('(')) return;
        
        Parametres();
        
        if (!consommer(')')) return;
        if (!consommer('{')) return;
        
        Corps();
        
        if (!consommer('}')) return;
    }
    
    /**
     * Parametres → ParamDecl AutresParam | ε
     */
    public static void Parametres() {
        if (currentToken == 'T') {
            ParamDecl();
            AutresParam();
        }
    }
    
    /**
     * AutresParam → , ParamDecl AutresParam | ε
     */
    public static void AutresParam() {
        if (currentToken == ',') {
            consommer(',');
            ParamDecl();
            AutresParam();
        }
    }
    
    /**
     * ParamDecl → Type ID
     * Déclaration de paramètre (sans point-virgule)
     */
    public static void ParamDecl() {
        Type();
        if (!consommer('I')) return;
    }
    
    /**
     * MAIN → Type main ( ) { Corps }
     */
    public static void MAIN() {
        Type();
        if (!consommer('M')) return;
        if (!consommer('(')) return;
        if (!consommer(')')) return;
        if (!consommer('{')) return;
        
        Corps();
        
        if (!consommer('}')) return;
    }
    
    /**
     * Corps → Instruction Corps | ε
     */
    public static void Corps() {
        while (currentToken != '}' && currentToken != '\0') {
            Instruction();
            
            if (!analyseSyntaxiqueReussie) {
                break;
            }
        }
    }
    
    /**
     * Instruction → Declaration | Affectation | BoucleWhile | AppelFonction | Return
     */
    public static void Instruction() {
        if (currentToken == 'T') {
            Declaration();
        } else if (currentToken == 'I') {
            int savePos = position;
            avancer();
            
            if (currentToken == '=') {
                position = savePos;
                currentToken = tokens.get(position);
                Affectation();
            } else if (currentToken == '(') {
                position = savePos;
                currentToken = tokens.get(position);
                AppelFonction();
            } else {
                System.err.println("Erreur : attendu '=' ou '(' apres ID");
                analyseSyntaxiqueReussie = false;
                position = savePos;
                currentToken = tokens.get(position);
            }
        } else if (currentToken == 'W') {
            BoucleWhile();
        } else if (currentToken == 'R') {
            Return();
        } else {
            System.err.println("Instruction non reconnue : token '" + currentToken + "'");
            analyseSyntaxiqueReussie = false;
            avancer();
        }
    }
    
    /**
     * Declaration → Type ID Initialisation Decla' ;
     */
    public static void Declaration() {
        Type();
        if (!consommer('I')) return;
        
        Initialisation();
        DeclaPrime();
        
        if (!consommer(';')) return;
    }
    
    /**
     * Decla' → , ID Initialisation Decla' | ε
     */
    public static void DeclaPrime() {
        if (currentToken == ',') {
            consommer(',');
            if (!consommer('I')) return;
            Initialisation();
            DeclaPrime();
        }
    }
    
    /**
     * Initialisation → = NUM | ε
     */
    public static void Initialisation() {
        if (currentToken == '=') {
            consommer('=');
            if (!consommer('N')) return;
        }
    }
    
    /**
     * Affectation → ID = Equa ;
     */
    public static void Affectation() {
        if (!consommer('I')) return;
        if (!consommer('=')) return;
        
        Equa();
        
        if (!consommer(';')) return;
    }
    
    /**
     * AppelFonction → ID ( Arguments ) ;
     */
    public static void AppelFonction() {
        if (!consommer('I')) return;
        if (!consommer('(')) return;
        
        Arguments();
        
        if (!consommer(')')) return;
        if (!consommer(';')) return;
    }
    
    /**
     * Arguments → Equa AutresArgs | ε
     */
    public static void Arguments() {
        if (currentToken == 'I' || currentToken == 'N' || currentToken == '(') {
            Equa();
            AutresArgs();
        }
    }
    
    /**
     * AutresArgs → , Equa AutresArgs | ε
     */
    public static void AutresArgs() {
        if (currentToken == ',') {
            consommer(',');
            Equa();
            AutresArgs();
        }
    }
    
    /**
     * BoucleWhile → while ( Cond ) { Corps }
     */
    public static void BoucleWhile() {
        if (!consommer('W')) return;
        if (!consommer('(')) return;
        
        Cond();
        
        if (!consommer(')')) return;
        if (!consommer('{')) return;
        
        Corps();
        
        if (!consommer('}')) return;
    }
    
    /**
     * Return → return Equa ;
     */
    public static void Return() {
        if (!consommer('R')) return;
        
        Equa();
        
        if (!consommer(';')) return;
    }
    
    /**
     * Equa → Terme Equa'
     */
    public static void Equa() {
        Terme();
        EquaPrime();
    }
    
    /**
     * Equa' → + Terme Equa' | - Terme Equa' | * Terme Equa' | / Terme Equa' | ε
     */
    public static void EquaPrime() {
        if (estDans(currentToken, '+', '-', '*', '/')) {
            char op = currentToken;
            consommer(op);
            Terme();
            EquaPrime();
        }
    }
    
    /**
     * Terme → ID | NUM | ( Equa ) | ID ( Arguments )
     */
    public static void Terme() {
        if (currentToken == 'I') {
            consommer('I');
            // Vérifier si c'est un appel de fonction
            if (currentToken == '(') {
                consommer('(');
                Arguments();
                consommer(')');
            }
        } else if (currentToken == 'N') {
            consommer('N');
        } else if (currentToken == '(') {
            consommer('(');
            Equa();
            consommer(')');
        } else {
            System.err.println("Erreur : terme attendu (ID, NUM ou expression)");
            analyseSyntaxiqueReussie = false;
        }
    }
    
    /**
     * Cond → CondSimple Cond'
     */
    public static void Cond() {
        CondSimple();
        CondPrime();
    }
    
    /**
     * Cond' → && CondSimple Cond' | || CondSimple Cond' | ε
     */
    public static void CondPrime() {
        if (currentToken == '&') {
            consommer('&');
            if (!consommer('&')) return;
            CondSimple();
            CondPrime();
        } else if (currentToken == '|') {
            consommer('|');
            if (!consommer('|')) return;
            CondSimple();
            CondPrime();
        }
    }
    
    /**
     * CondSimple → Equa OpComp Equa | Equa
     */
    public static void CondSimple() {
        Equa();
        
        if (estDans(currentToken, '<', '>', '=', '!')) {
            OpComp();
            Equa();
        }
    }
    
    /**
     * OpComp → == | != | > | < | >= | <=
     */
    public static void OpComp() {
        if (currentToken == '=') {
            consommer('=');
            if (!consommer('=')) return;
        } else if (currentToken == '!') {
            consommer('!');
            if (!consommer('=')) return;
        } else if (currentToken == '>') {
            consommer('>');
            if (currentToken == '=') {
                consommer('=');
            }
        } else if (currentToken == '<') {
            consommer('<');
            if (currentToken == '=') {
                consommer('=');
            }
        } else {
            System.err.println("Erreur : operateur de comparaison attendu");
            analyseSyntaxiqueReussie = false;
        }
    }
    
    /**
     * Type → int | float | void
     */
    public static void Type() {
        if (!consommer('T')) return;
    }
    
    /**
     * Effectue l'analyse syntaxique
     */
    public static boolean analyseSyntaxique(String fichierTokens) {
        System.out.println("========================================================");
        System.out.println("        PHASE 2 : ANALYSE SYNTAXIQUE                   ");
        System.out.println("========================================================\n");
        
        try {
            System.out.println("Lecture des tokens depuis : " + fichierTokens);
            lireTokens(fichierTokens);
            
            if (tokens.isEmpty()) {
                System.err.println("Aucun token a analyser !");
                return false;
            }
            
            System.out.println("Debut de l'analyse syntaxique...\n");
            
            // Lancer l'analyse syntaxique
            Programme();
            
            if (analyseSyntaxiqueReussie) {
                System.out.println("\nAnalyse syntaxique reussie !\n");
            } else {
                System.out.println("\nAnalyse syntaxique echouee !\n");
            }
            
        } catch (IOException e) {
            System.err.println("Erreur de lecture du fichier : " + e.getMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Erreur inattendue : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        
        return analyseSyntaxiqueReussie;
    }
    
    // ==================== AFFICHAGE DE LA GRAMMAIRE ====================
    
    /**
     * Affiche les règles de production de la grammaire
     */
    public static void afficherGrammaire() {
        System.out.println("\n========================================================");
        System.out.println("           REGLES DE PRODUCTION DE LA GRAMMAIRE        ");
        System.out.println("========================================================\n");
        
        System.out.println("Programme -> #include<Directive> ListeFonctions MAIN");
        System.out.println("ListeFonctions -> Fonction ListeFonctions | epsilon");
        System.out.println("Fonction -> Type ID ( Parametres ) { Corps }");
        System.out.println("Parametres -> ParamDecl AutresParam | epsilon");
        System.out.println("AutresParam -> , ParamDecl AutresParam | epsilon");
        System.out.println("ParamDecl -> Type ID");
        System.out.println("MAIN -> Type main ( ) { Corps }");
        System.out.println("Corps -> Instruction Corps | epsilon");
        System.out.println("Instruction -> Declaration | Affectation | BoucleWhile | AppelFonction | Return");
        System.out.println("Declaration -> Type ID Initialisation Decla' ;");
        System.out.println("Decla' -> , ID Initialisation Decla' | epsilon");
        System.out.println("Initialisation -> = NUM | epsilon");
        System.out.println("Affectation -> ID = Equa ;");
        System.out.println("AppelFonction -> ID ( Arguments ) ;");
        System.out.println("Arguments -> Equa AutresArgs | epsilon");
        System.out.println("AutresArgs -> , Equa AutresArgs | epsilon");
        System.out.println("BoucleWhile -> while ( Cond ) { Corps }");
        System.out.println("Return -> return Equa ;");
        System.out.println("Equa -> Terme Equa'");
        System.out.println("Equa' -> + Terme Equa' | - Terme Equa' | * Terme Equa' | / Terme Equa' | epsilon");
        System.out.println("Terme -> ID | NUM | ( Equa ) | ID ( Arguments )");
        System.out.println("Cond -> CondSimple Cond'");
        System.out.println("Cond' -> && CondSimple Cond' | || CondSimple Cond' | epsilon");
        System.out.println("CondSimple -> Equa OpComp Equa | Equa");
        System.out.println("OpComp -> == | != | > | < | >= | <=");
        System.out.println("Type -> int | float | void");
        System.out.println("Directive -> stdio.h");
        System.out.println("\n========================================================");
        System.out.println("CODES DES TOKENS:");
        System.out.println("  T = Type (int, float, void, char)");
        System.out.println("  I = Identifiant");
        System.out.println("  N = Nombre");
        System.out.println("  M = main");
        System.out.println("  W = while");
        System.out.println("  R = return");
        System.out.println("  C = include");
        System.out.println("  B = stdio.h");
        System.out.println("  Operateurs: + - * / = ; , ( ) { } < > ...");
        System.out.println("========================================================");
        System.out.println("OBLIGATION DE LAISSAIS DES ESPASSE ENTRE LES MOT");
        System.out.println("ex: #include<stdio.h> -----> faux");
        System.out.println("ex: # include < stdio.h > -----> vrai");
        System.out.println("   ^ ^       ^ ^       ^            ");
        System.out.println("les espasse");
        System.out.println("========================================================\n");
    }
    
    // ==================== MAIN ====================
    
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        
        System.out.println("========================================================");
        System.out.println("   ANALYSEUR LEXICAL + SYNTAXIQUE COMPLET              ");
        System.out.println("              (Descente Recursive)                      ");
        System.out.println("========================================================\n");
        
        System.out.println("MENU PRINCIPAL:");
        System.out.println("1. Afficher les regles de production");
        System.out.println("2. Tester avec un fichier test.c predifini");
        System.out.println("3. Choisir un fichier source");
        System.out.print("\nVotre choix (1/2/3) : ");
        
        String choix = sc.nextLine();
        
        if (choix.equals("1")) {
            afficherGrammaire();
            System.out.print("Appuyez sur Entree pour continuer...");
            sc.nextLine();
            System.out.println("\nVoulez-vous continuer avec l'analyse ? (o/n) : ");
            String continuer = sc.nextLine();
            if (!continuer.equalsIgnoreCase("o")) {
                sc.close();
                return;
            }
            System.out.println("\n1. Tester avec test.c");
            System.out.println("2. Choisir un fichier");
            System.out.print("Votre choix (1/2) : ");
            choix = sc.nextLine();
        }
        
        String fichierSource;
        String fichierTokens;
        
        if (choix.equals("2")) {
            // Fichier test prédéfini
            fichierSource = "test.c";
            fichierTokens = "tokens.txt";
            System.out.println("\nUtilisation du fichier test.c");
            System.out.println("Tokens seront ecrits dans tokens.txt\n");
        } else {
            // Choisir un fichier
            System.out.print("\nChemin du fichier source (.c) : ");
            fichierSource = sc.nextLine();
            fichierTokens = "tokens.txt";
        }
        
        System.out.println();
        
        // Phase 1 : Analyse Lexicale
        boolean lexicaleOK = analyseLexicale(fichierSource, fichierTokens);
        
        if (!lexicaleOK) {
            System.err.println("Analyse arretee : erreurs lexicales detectees");
            sc.close();
            return;
        }
        
        // Phase 2 : Analyse Syntaxique
        boolean syntaxiqueOK = analyseSyntaxique(fichierTokens);
        
        // Résultat final
        System.out.println("========================================================");
        if (lexicaleOK && syntaxiqueOK) {
            System.out.println("          COMPILATION REUSSIE !                        ");
            System.out.println("   Le programme est lexicalement et                    ");
            System.out.println("   syntaxiquement correct                              ");
        } else if (lexicaleOK && !syntaxiqueOK) {
            System.out.println("          ERREURS SYNTAXIQUES                          ");
            System.out.println("   Analyse lexicale OK, syntaxique KO                  ");
        } else {
            System.out.println("          COMPILATION ECHOUEE                          ");
        }
        System.out.println("========================================================");
        
        sc.close();
    }
}