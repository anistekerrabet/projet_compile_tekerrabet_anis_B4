# include< stdio.h >
int calculer ( int a , int b ) {
    int resultat ;
    resultat = a + b * 2 ;
    return resultat ;
}

int factorielle ( int n ) {
    int fact ;
    fact = 1 ;
    int i ;
    i = 1 ;
    while ( i < n ) {
        fact = fact * i ;
        i = i + 1 ;
    }
    return fact ;
}

int fibonacci ( int n ) {
    int a , b , temp ;
    a = 0 ;
    b = 1 ;
    int compteur ;
    compteur = 0 ;
    while ( compteur < n ) {
        temp = a + b ;
        a = b ;
        b = temp ;
        compteur = compteur + 1 ;
    }
    return a ;
}

void afficher ( int x , int y ) {
    int somme ;
    somme = x + y ;
    return 0 ;
}

int main ( ) {
    int nombre1 , nombre2 , nombre3 ;
    nombre1 = 10 ;
    nombre2 = 20 ;
    nombre3 = 5 ;
    
    int resultat1 ;
    resultat1 = calculer ( nombre1 , nombre2 ) ;
    
    int resultat2 ;
    resultat2 = factorielle ( nombre3 ) ;
    
    int resultat3 ;
    resultat3 = fibonacci ( 8 ) ;
    
    afficher ( resultat1 , resultat2 ) ;
    
    int total ;
    total = resultat1 + resultat2 + resultat3 ;
    
    int test ;
    test = 100 ;
    while ( test >= 0 ) {
        test = test - 5 ;
    }
    
    int a , b , c , d ;
    a = 1 ;
    b = 2 ;
    c = 3 ;
    d = a + b * c - d / 2 ;
    
    return 0 ;
}