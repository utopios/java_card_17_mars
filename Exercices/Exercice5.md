###  Exercice 5 **Chiffrement AES – Exercice : Badge d’accès sécurisé**

**Sujet :**  
Implémente une applet qui chiffre un identifiant utilisateur avec une clé AES 128 bits stockée en mémoire. Lors de la présentation du badge (simulation par APDU), l’applet chiffre l’ID et retourne le bloc chiffré. Un système distant (client Java) le déchiffre pour authentifier l’accès.

**Objectif pédagogique :**  
- Gérer la clé AES dans la carte  
- Travailler en mode CBC avec des données paddées  
- Associer chiffrement AES et contrôle d’accès
