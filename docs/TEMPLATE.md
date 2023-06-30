# Progetto XYZ - Liferay Workspace per Liferay 7.4 CE e DXP

**Da usare come base di partenza per README.md del vostro workspace.**

**TODO**: Breve descrizione del progetto

Ricordatevi di modificare il file build.gradle (cerca `**change-me**`) per indicare il 
group con cui i vostri artefatti saranno riconosciuti in https://repository.smc.it
o nel Nexus Repository configurato. E di indicarlo anche in questo file.

**TODO**: Vagliare la sezione Note di Sviluppo lasciando quella coerente con il progetto

**TODO: Dichiarare gli aspetti base di compatibilita' di approccio allo sviluppo. Segue esempio**

<hr>

Alcuni dettagli importanti:
* questo repository gestisce e mantiene la compatibilit&agrave; sia con la versione CE che DXP di Liferay
* in questo branch la compatibilit&agrave; base, gestita tramite BOM, &egrave;
    - Liferay 7.4 CE GA1
    - Liferay 7.4 DXP GA1
* eventuali problematiche di compatibilit&agrave; con GA o fixpack successivi dovrannno
essere gestiti attraverso nuovi artefatti mirati
* il *group* di pubblicazione degli artefatti su [Nexus](https://registry.smc.it)
&egrave; `** TODO: deve essere uguale a quanto indicato nel build.gradle **`.

Inoltre questa soluzione **deve** essere sviluppata come se dovesse essere:
* pubblicabile sul [Liferay Marketplace](https://web.liferay.com/it/marketplace)
* installabile su un bundle Liferay
* installabile su un Liferay con [OpenSquare Spaces](https://git.smc.it/opensquare/os-spaces-workspace)

Ne consegue che lo sviluppo deve essere svolto in modo attento per non inquinare
gli artefatti di dipendenze o vincoli che ne impediscano le opportunit&agrave; citate
sopra.<br/>

Un uso sapiente dei *Profili di Distribuzione* pu&ograve; aiutare a raggiungere questo scopo.

<hr>


## APPUNTI UTILI

### Git e GitLab

La lingua del progetto &egrave; `**TODO**`. Da usare nei commenti dei commit, nelle issue e nei wiki.

Per gestire i diversi sistemi operativi degli sviluppatori ricordatevi di configurare il vostro
ambiente  per gestire correttamente il terminatore di riga (il default &egrave; Unix).

Potete farlo a livello globale:

	git config --global core.eol lf
	git config --global core.autocrlf input


### La cartella modules

La cartella `modules` contiene i bundle del nostro progetto alcuni dei
quali e' preferibile raggruppare in particolari sotto-cartelle come descritto
nel [README.md](./modules/README.md) presente

### La cartella themes

La cartella `themes` contiene il file `build.gradle.theme.sample` che dovrebbe essere
copiato, come `build.gradle` nei moduli di tipo Tema o LayouTemplate.<br/>
Lo scopo di questo file e' di consentire l'upload su Nexus dell'artefatto

### Copyright ed altri aspetti Legali

Dal punto di vista legale deve essere ben chiara ed esplicitata la licenza con cui
viene distribuito il software. Deve essere chiaro a tutti i programmatori le implicazioni
legali nell'utilizzo di librerie di terzi o nell'incorporazione di sorgenti facenti
parte di Liferay.<br/>
La contaminazione della licenza ha impatti sulla composizione degli artefatti e sulla
presenza dei sorgenti.

Dal punto di vista tecnico i plugin dovranno essere consistenti. Ci&ograve; significa
che un plugin pu&ograve; essere composto da pi&ugrave; artefatti ma questi devono
essere facilmente identificabili e non sconfinare in altri plugin.
Sono ammessi gli artefatti "core" o "shared".

Siete pregati di utilizzare le cartelle desritte in [README.md](./modules/README.md)
per gestire / isolare i bundle delicati

### Codice di Qualit&agrave;

Dovrebbe essere una cosa normale, ma anche come conseguenza del fatto che SMC &egrave;
un "Liferay Platinum Partner", i sorgenti che scriviamo devono:
* rispettare tutte le best practices Liferay
* rispettare le code convention di Liferay

Ulteriore appunto sul file "gradle.properties". Questo file conterr&agrave; le impostazioni
standard del repository e **DEVE** essere versionato e **NON DEVE** essere sporcato da
impostazioni locali.<br/>
Le impostazioni locali del proprio workspace, come la posizione del bundle Liferay,
devono essere indicate nel file "gradle-local.properties" che **NON DEVE** e
**NON SARA'** versionato.


## Note di Sviluppo (Caso 1: Progetto Cliente)

Questo repository adotta le regole della "[Trunk Based Development](https://git.smc.it/guidelines/development/blob/master/Progetti_Liferay/00_index.md)". 
Trattandosi di un "Progetto Cliente" si applica una logica simil "Master Rolling Release".


Significa che:

* gli sviluppo sono sempre realizzati in una o piu' feature-branch create a partire dal master. Branch che utilizzano la nomanclatura `<numero-issue>-breve-testo`
    - il feature branch &agrave; in grado di produrre artefatti SNAPSHOT univoci, come pure una distribuzione completa grazie al task `zipDistAll`.
    - test e review possono e devono essere fatti nel feature branch usando gli artefatti SNAPSHOT prodotti dal feature branch. 
      Sicuramente i test interni SMC. In alcuni casi anche i test nell'ambiente di test del cliente

* il feature branch viene consolidato nel master che produce artefatti RELEASED. La fase di merge prevede che:
    - la feature branch abbia subito un rebase o un merge del codice presente nel master
    - tutti i moduli variati nella feature-branch abbiano la versione avanzata nella parte micro, o minor, o major, come da logiche semantic version
    - al completamento del merge i moduli siano pubblicati sul Nexus di SMC

* la "stabilit&agrave;" del master &egrave; responsabilit&agrave; del Maintainer del progetto. E' sua responsabilit&agrave; far confluire nel master solo gli 
  sviluppi che considera elegibili ad una potenziale consegna

* il rilascio del software consiste nella emissione di un tag, sul master, che congeli l'istante e permetta di replicare l'output del comando zipDistAll
    - il nome del tag sar&agrave; nel formato `rel_X.Y.Z`, o `rel_X.Y.Z-rc1`, o `rel_X.Y.Z-rc2`, o assimilabili
    - il nome del tag sar&agrave; coerente con la propriet&agrave; `product.version` presente nel file `gradle.properties`.

* la pubblicazione degli artefatti su Nexus non &agrave; obbligatoria. La coerenza e la riproducibilit&agrave; dei rilasci &agrave; garantita dai tag e dagli script 
  che compongono il template SMC. Si pu&ograve; comunque effettuare


Per completezza la procedura di rilascio consiste in:
* verifico ed aggiorno la propriet&agrave; `product.version` nel `gradle.properties`
    - consolido questa attivit&agrave; con un commit il cui commento sar&agrave; "Pronti per la release X.Y.X"
* creo il tag di release sul repository. Il tag praticamente sar&agrave; sul commit eseguito al punto precedente
* aggiorno la propriet&agrave; `product.version` nel `gradle.properties`
    - devo esplicitare che da adesso in poi quello che sar&agrave; fatto nel master coinfluir&agrave; nel prossimo rilascio
    - se ho appena rilasciato la "1.1.3", la versione da indicare sar&agrave; "1.1.4-beta"
    - consolido il tutto con un commit il cui commenot sar&agrave; "Predisposto per prossima release"
 

## Note di Sviluppo (Caso 2: Progetto Prodotto o Progetto Feature)

Questo repository adotta le regole della "[Trunk Based Development](https://git.smc.it/guidelines/development/blob/master/Progetti_Liferay/00_index.md)". 


Significa che:

* gli sviluppo sono sempre realizzati in una o piu' feature-branch create a partire dal master (in caso di intervento evolutivo) o dall'apposito branch
  release (in caso di intervento correttivo). Branch che utilizzano la nomanclatura `<numero-issue>-breve-testo`
    - il feature branch &agrave; in grado di produrre artefatti SNAPSHOT univoci, come pure una distribuzione completa grazie al task `zipDistAll`.
    - test e review possono e devono essere fatti nel feature branch usando gli artefatti SNAPSHOT prodotti dal feature branch. 
      Sicuramente i test interni SMC. In alcuni casi anche i test nell'ambiente di test del cliente

* il branch master produce sempre artefatti SNAPSHOT in quanto ospita le evoluzioni della prossima versione. E per definizione una evoluzione &egrave;
  "work in progress" finch&egrave; non inizia la sua fase di rilascio

* i branch "release/v0.0.x" producono sempre artefatti RELEASE in quanto ospita le correzioni e deve essere sempre pronto ad un rilascio in tempi stretti.
 
* il feature branch viene consolidato nel branch di riferimento (master o release/v0.0.x). La fase di merge prevede che:
    - la feature branch abbia subito un rebase o un merge del codice presente nel branch di riferimento
    - tutti i moduli variati nella feature-branch abbiano la versione avanzata nella parte micro, o minor, o major, come da logiche semantic version
    - al completamento del merge i moduli siano pubblicati sul Nexus di SMC

* la "stabilit&agrave;" del master e dei branch "release/v0.0.x" &egrave; responsabilit&agrave; del Maintainer del progetto. E' sua responsabilit&agrave; 
  farvi confluiresolo gli sviluppi che considera elegibili ad una potenziale consegna

* il rilascio di una nuova release "major.minor" inizia con la creazione dell'apposito branch di release

* il rilascio della GA o di ua release manutentiva parte sempre da un branch di release precedentemente creato

* il rilascio del software consiste nella emissione di un tag, sul branch di rlease, che congeli l'istante e permetta di replicare l'output del comando zipDistAll
    - il nome del tag sar&agrave; nel formato `rel_X.Y.Z`, o `rel_X.Y.Z-rc1`, o `rel_X.Y.Z-rc2`, o assimilabili
    - il nome del tag sar&agrave; coerente con la propriet&agrave; `product.version` presente nel file `gradle.properties`.

* la pubblicazione degli artefatti su Nexus non &agrave; obbligatoria. La coerenza e la riproducibilit&agrave; dei rilasci &agrave; garantita dai tag e dagli script 
  che compongono il template SMC. Si pu&ograve; comunque effettuare


Per completezza la procedura di rilascio consiste in:
* verifico ed aggiorno la propriet&agrave; `product.version` nel `gradle.properties`
    - consolido questa attivit&agrave; con un commit il cui commento sar&agrave; "Pronti per la release X.Y.X"
* creo il tag di release sul repository. Il tag praticamente sar&agrave; sul commit eseguito al punto precedente
* aggiorno la propriet&agrave; `product.version` nel `gradle.properties`
    - devo esplicitare che da adesso in poi quello che sar&agrave; fatto nel master coinfluir&agrave; nel prossimo rilascio
    - se ho appena rilasciato la "1.1.3", la versione da indicare sar&agrave; "1.1.4-beta"
    - consolido il tutto con un commit il cui commenot sar&agrave; "Predisposto per prossima release"
 


