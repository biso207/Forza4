Version Beta 1.4.1

* migliorata l'ombra del pulsante YES e NO delle schermate di conferma 'logout' e 'quit game'
* migliorato il pulsante rosso per l'avvio e accesso al gioco delle schermate di autenticazione
* aggiunto un testo informativo nella schermata SignUp per indicare all'utente che la data di registrazione è
necessaria per resettare la password
* bloccato il click del pulsante rosso per l'avvio e accesso al gioco fino a quando non vengono digitati nickname e 
password. è analogo all'effetto hover che già era in funzione
* modificata la posizione dei testi nella schermata SignUp e ResetPassword
* risolto il bug del crash al reset della password come prima operazione eseguita all'apertura del gioco. i progressi 
utente non venivano mai caricati in memoria e questo creava un crash con il recupero di un valore come "null", se 
l'utente avesse fatto l'accesso o fosse stato creato un utente, e successivamente si effettuava il reset della 
password, allora andava tutto perché i progressi erano già stati caricati in memoria dopo l'accesso e/o la registrazione
