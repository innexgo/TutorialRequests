import sqlite3
import json

supression_list_file = 'account_supression_list.json'

supression_list = open(supression_list_file, 'r')
supression_raw_json = json.loads(supression_list.read())

supression_emails = supression_raw_json['SuppressedDestinationSummaries']

database = sqlite3.connect('../hours.db')

class id_generator():
    def __init__(self):
        id_fetch = database.execute('SELECT max(id) FROM email_blacklist').fetchone()[0]
        if id_fetch == None:
            self.current_id = 0
        else:
            self.current_id = int(id_fetch)
    
    def __next__(self):
        return self.next()

    def next(self):
        self.current_id += 1
        return self.current_id

id_number = id_generator()

for emailObject in supression_emails:
    email_check = database.execute('SELECT count(*) FROM email_blacklist WHERE email=?', (emailObject['EmailAddress'],)).fetchone()[0]
    if int(email_check) == 0:
        database.execute('INSERT INTO email_blacklist (id, email, reason, last_update_time) VALUES (?, ?, ?, ?)',
                (next(id_number), emailObject['EmailAddress'], emailObject['Reason'], emailObject['LastUpdateTime']))

check = database.execute('SELECT * FROM email_blacklist').fetchall()
print(check)
check2 = database.execute('SELECT * FROM email_blacklist WHERE email=?', ('aiusgriuqawhgor',)).fetchall()
print(check2)

database.commit()