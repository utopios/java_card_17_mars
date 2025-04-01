## 🔐 **TP JavaCard – Authentification Multi-Facteur pour Contrôle d’Accès Physique Sécurisé**

### 🎯 **Contexte professionnel réel :**
Une entreprise de haute technologie (R&D, défense, finance) souhaite renforcer l’accès à ses bâtiments en combinant **badge physique + mot de passe**. Le badge JavaCard doit contenir :
- des informations d’identification cryptées,
- un mécanisme de vérification de mot de passe embarqué (PIN),
- une signature de chaque session d’entrée pour traçabilité.

Chaque employé passe son badge sur un terminal sécurisé qui :
1. Demande le mot de passe (via clavier ou smartphone)
2. Envoie le mot de passe à la carte pour vérification
3. Demande à la carte de signer une session d'accès (timestamp)
4. Vérifie la signature côté serveur

---

### 🧩 **Fonctionnalités attendues de la carte (Applet JavaCard)** :
- **Stocker un identifiant utilisateur (UID)**
- **Stocker un hash SHA-256 du mot de passe (ou PIN)**
- **Permettre la vérification d’un mot de passe reçu via APDU**
- **Signer un timestamp (challenge) avec la clé privée RSA ou ECDSA**
- **Ne pas exposer la clé privée**
- **Bloquer la carte après 3 tentatives échouées**

---

### 📜 **Instructions APDU** :
| CLA | INS | Description                     |
|-----|-----|---------------------------------|
| 80  | 20  | Vérification du mot de passe    |
| 80  | 30  | Signature de session (timestamp)|
| 80  | 40  | Initialisation de la carte (enrôlement UID + PIN) |