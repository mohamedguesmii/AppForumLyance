from envoyer_evenement import scrape_meetup, envoyer_evenement_formdata

if __name__ == "__main__":
    evenements = scrape_meetup()
    for event in evenements:
        envoyer_evenement_formdata(event)
