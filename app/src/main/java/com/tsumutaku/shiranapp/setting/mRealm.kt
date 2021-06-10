package com.tsumutaku.shiranapp.setting


import io.realm.Realm


class mRealm {

    val person: Person = Person()
    val realm = Realm.getDefaultInstance()

    fun addPerson(Name: String, AcUid: String) {
        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }

        /*if (person == null) {
            // 新規作成の場合
            person = Person()
        }
         */

        val taskRealmResults = realm.where(Person::class.java).findAll()

        val identifier: Int =
            if (taskRealmResults.max("id") != null) {
                taskRealmResults.max("id")!!.toInt() + 1
            } else {
                0
            }
        person.id = identifier
        person.Name = Name
        person.AcUid = AcUid
        realm.copyToRealmOrUpdate(person)
        realm.commitTransaction()
        realm.close()
    }

    fun SearchName(SearchedName: String?): Person {
        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }
        val taskRealmResults = realm.where(Person::class.java).findAll()
        for (i in taskRealmResults) {
            if (i.Name == SearchedName) {
                val info = Person(i.id, i.Name, i.AcUid)
                return info
            }
        }
        realm.close()
        val info = Person(-1, "完全一致する名前が\n見つかりません", "")
        return info
    }

    //ユーザーIDからを名前取得
    fun UidToName(SearchedUid: String): String {
        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }
        val taskRealmResults = realm.where(Person::class.java).findAll()
        for (i in taskRealmResults) {
            if (i.AcUid == SearchedUid) {
                return i.Name
            }
        }
        realm.close()
        return ""
    }
    //名前からをユーザーID取得
    fun NameToUid(SearchedName: String): String {
        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }
        val taskRealmResults = realm.where(Person::class.java).findAll()
        for (i in taskRealmResults) {
            if (i.Name == SearchedName) {
                return i.AcUid
            }
        }
        realm.close()
        return ""
    }

    //同じ名前がすでに使われていれば、false、使える名前ならtrue
    fun SearchSameName(SearchedName: String): Boolean {
        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }
        val taskRealmResults = realm.where(Person::class.java).findAll()
        for (i in taskRealmResults) {
            if (i.Name == SearchedName) {

                return false
            }
        }
        realm.close()
        return true
    }

    fun update(id: String, name: String) {
        if (!realm.isInTransaction) {
            realm.beginTransaction()
        }
        var book = realm.where(Person::class.java).equalTo("AcUid", id).findFirst()
        book!!.Name = name
        //realm.executeTransaction {        }
        realm.close()
    }
}
