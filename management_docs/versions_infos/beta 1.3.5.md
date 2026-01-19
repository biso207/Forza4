Version Beta 1.3.5

* corretta la difficoltà base di gioco a 1 per i nuovi utenti. precedentemente veniva assegnata a 2

* risolto il bug relativo al lock della sessione utente, ora si può giocare con lo stesso utente su un solo dispositivo
alla volta. alla chiusura del gioco e al logout utente il lock viene rilasciato

* cambiato il colore della barra di caricamento per la schermata di caricamento 3

* impostato il formatter per il testo dei crediti utente in lobby

* risolto il bug della doppia scrittura del titolo nelle pagine di gioco

* implementata la logica del market e la sua grafica
  * aggiunto il prezzo per ogni elemento che si aggiorna in base al numero di elementi selezionato da comprare 
  * implementata la digitazione a tastiera del numero di elementi e fissato il massimo in base ai crediti
  disponibili e al prezzo dell'elemento
  * implementato l'hover sui pulsanti di acquisto
  * implementato il click sui pulsanti
  * implementata la diminuzione dei crediti all'acquisto di un elemento
  * aggiungere il numero di boost acquistati
  * bloccata la grafica di hover e click sui pulsanti di acquisto se non si hanno abbastanza crediti per procedere con l'acquisto

* aggiunta la nuova musica di startup
