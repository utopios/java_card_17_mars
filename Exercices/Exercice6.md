###  2. **Chiffrement RSA – Exercice : Boîte aux lettres confidentielle**

**Sujet :**  
Crée une applet qui reçoit un message chiffré par le client avec la clé publique RSA de la carte (ex. un message personnel). L’applet le déchiffre avec sa clé privée et retourne le contenu en clair. La carte agit comme une boîte aux lettres que seul le porteur peut consulter.

L'applet permet de renvoyer la clé public dans un premier temps.


**Objectif pédagogique :**  
- Comprendre la séparation clé publique/privée  
- Manipuler le chiffrement RSA PKCS#1 v1.5  
- Gérer les APDU avec un message fragmenté si besoin