### Exercice 4: "Communication sécurisée entre applets via Shareable"

#### Objectif
Créer deux applets sur Java Card :
- **SecureCounterApplet** : conserve un compteur sécurisé.
- **MonitorApplet** : peut interroger et remettre à zéro ce compteur via une interface Shareable.

#### Contraintes
1. Le `SecureCounterApplet` doit :
   - Augmenter le compteur à chaque appel.
   - Offrir une méthode sécurisée (via `Shareable`) permettant à un applet autorisé de :
     - Lire la valeur actuelle.
     - Réinitialiser le compteur.

2. Le `MonitorApplet` doit :
   - Appeler le `SecureCounterApplet` via `JCSystem.getAppletShareableInterfaceObject()`.
   - Afficher la valeur du compteur.
   - Réinitialiser le compteur si celle-ci dépasse un seuil fixé (ex : 10).

#### Extensions possibles
- Ajouter une vérification d’identité via l’AID avant d’accepter le partage.
- Ajouter une politique de contrôle d’accès (paramètre dans `getShareableInterfaceObject()`).
