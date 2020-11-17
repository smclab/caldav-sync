# STRUTTURA SUGGERITA

All'interno della cartella modules trovano posto diversi bundle che compongono
il progetto.

Alcuni di questi sono specializzati in particolari azioni ed e' quindi 
altamente consigliato posizionarli all'interno di cartelle _parlanti_:
```
modules/
  liferay-modules/
  portal-fixes/
  portal-hooks/
  portal-overrides/
```


## Liferay Modules

Alcuni moduli Liferay CE o DXP, pur essendo presenti all'interno dei
sorgenti del progetto [liferay-portal](https://github.com/liferay/liferay-portal)
(il progetto dei sorgenti presente su github), non sono disponibili come
artefatto compilato.

Tali moduli, se necessari per il progetto, devono essere copiati
all'interno della cartella ```liferay-modules``` dalla versione GA/FixPack 
di riferimento per essere compilati. Le uniche modifiche consentite 
sono quelle nel file ```gradle.properties``` per consentire la compila nel 
modo corretto. 


## Portal Fixes

Nella cartella ```portal-fixes``` troveranno posto i moduli relativi al 
backporting di fix dal branch master alla versione GA usata. Man mano che 
la soluzione avanza di GA questi moduli saranno deprecati e rimossi.

In presenza di bug non risolti neanche nel master si chiede al programmatore di:
* individuare il problema sulla GA e produrre un workaround
* creare la LPS in [Liferay Issues](https://issues.liferay.com)
* suggerire il workround come commento, oppure realizzarlo sul master e fornirlo
come contribuzione in pull-request


## Portal Hooks

Nella cartella ```portal-hooks`` contiene le customizzazioni effettuate ai 
moduli forniti **a standard** da Liferay.

Per **moduli a standard** s'intendono tutti quelli presenti nel progetto
[liferay-portal](https://github.com/liferay/liferay-portal)
(il progetto dei sorgenti presente su github) che comprende anche la cartella 
con i moduli osgi (```modules```).

Tipicamente questa cartella conterrà:
* gli interventi sulla UI (modulo con suffisso ```-web-fragment```)
* gli interventi sui Component, ovvero realizzati sfruttando le potenzialità OSGi
(modulo con suffisso ```-hook```)

Salvo aspetti tecnici o di _Licenza d'Uso_, si consiglia di raggruppare gli 
interventi OSGi in un singolo artefatto.

Ovvero, se modifico una ```MVCActionCommand``` ed un ```Service``` della ```chat``` creo il 
modulo ```chat-hook``` e non due moduli ```chat-service-hook``` e ```chat-web-hook```.


## Portal Overrides

All'interno della cartella ```portal-overrides``` troveranno posto i moduli realizzati in modalità override.
È una specie di ext 6.2, con la differenza che è specifico del singolo bundle.
Per ulteriori informazioni vedere [Extending Liferay OSGi Modules](https://web.liferay.com/web/user.26526/blog/-/blogs/extending-liferay-osgi-modules).

L'intervento deve essere chirurgico.

Si usa in 2 casi.

**Per risolvere bug di prodotto per i quali:**

* esiste una fix nel trunk (se CE) o un fixpack (se DXP)
* esiste una LPS non risolta e per la quale si può usare il nostro codice per una contribuzione
* creeremo noi una LPS relativa

È buona norma indicare sempre la LPS del bug/fix nei commenti o nel file ```readme.md```

**Per sviluppi particolari:** ad esempio sostituire un portlet core che non usa le _MVCRender/MVCAction_.

L'importante è non abusare.

