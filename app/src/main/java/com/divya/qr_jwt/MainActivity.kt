package com.divya.qr_jwt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.divya.qr_jwt.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.zxing.integration.android.IntentIntegrator


class MainActivity : AppCompatActivity() {

    lateinit var name : String
    lateinit var email : String
    lateinit var phnum : String
    private lateinit var binding: ActivityMainBinding
    private lateinit var database : FirebaseDatabase
    private lateinit var databaseReference :DatabaseReference
    private lateinit var databaseReference1: DatabaseReference
    lateinit var count:TextView
    var count1:Int=0

    lateinit var firedaseauthetication : FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

            // Restore value of members from saved state
            //xcount = savedInstanceState.getString(count1)
        firedaseauthetication = FirebaseAuth.getInstance()
        firedaseauthetication.signInWithEmailAndPassword("divyamulchandani01@gmail.com","Kavita_12")
            .addOnCompleteListener() { task ->
                if (task.isSuccessful) {
                    //Toast.makeText(this, "Logged In", Toast.LENGTH_SHORT).show()
                }
        }
        database=FirebaseDatabase.getInstance()
        databaseReference = database.getReference().child("User")

        count=findViewById(R.id.count)
        val qrButton: ImageButton = findViewById(R.id.qr_button)
        qrButton.setOnClickListener{
            val intentIntegrator = IntentIntegrator(this)
            intentIntegrator.initiateScan()
            database=FirebaseDatabase.getInstance()
            databaseReference = database.getReference().child("User")
        }

        val databtn :ImageButton = findViewById(R.id.data)
        databtn.setOnClickListener {
            if (name.isNotEmpty()){
                //readdata
                readData(phnum)
            }
            else{
                Toast.makeText(this, "Name not recevied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun readData(phnum: String) {
        database=FirebaseDatabase.getInstance()
        databaseReference = database.getReference().child("User")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Use the snapshot variable here
                for (childSnapshot in dataSnapshot.children) {
                    // Do something with each child snapshot
                    childSnapshot.ref.child("food").setValue("no").addOnSuccessListener {
                        // Do something on success
                        Toast.makeText(this@MainActivity, "food value changed", Toast.LENGTH_SHORT).show()
                    }.addOnFailureListener {
                        // Handle the error
                    }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = IntentIntegrator.parseActivityResult(resultCode, data)
        val data = result.contents
        val decodedJWT:DecodedJWT
        val algorithm:Algorithm = Algorithm.HMAC256("neofolks2023")
        val verifier = JWT.require(algorithm).build()
        decodedJWT = verifier.verify(data)
        name = decodedJWT.getClaim("name").asString()
        email = decodedJWT.getClaim("email").asString()
        phnum = decodedJWT.getClaim("phone").asString()
        val teamname = decodedJWT.getClaim("teamName").asString()
        val textView2: TextView = findViewById(R.id.result1)
        textView2.text=name
        val textView3: TextView = findViewById(R.id.result2)
        textView3.text=email
        val textView4: TextView = findViewById(R.id.result3)
        textView4.text=phnum
        val textView5: TextView = findViewById(R.id.result4)
        textView5.text=teamname

//        database=FirebaseDatabase.getInstance()
//        databaseReference = database.getReference().child("User")

        databaseReference.child(phnum).child("phnum").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Use the snapshot variable here
//                println("***************************************")
//                println(dataSnapshot.getValue() == phnum)
//                println("***************************************")
                if (dataSnapshot.getValue()== phnum){
                    //Do nothing
                }else{
                    val user = User(name,email,phnum,teamname,"no")
                    databaseReference.push().setValue(phnum)
                        .addOnSuccessListener {
//                            Toast.makeText(this, "Name set", Toast.LENGTH_SHORT).show()
                            databaseReference.child(phnum).setValue(user)
                                .addOnSuccessListener {
                                    //Toast.makeText(this, "Data Uploaded Successfully!", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener {
//                            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })

        databaseReference.child(phnum).get().addOnSuccessListener {

            //for (childSnapshot in dataSnapshot.children) { }
            if (it.exists()){
                val food = it.child("food").value
                if (food=="Yes"){

                    val result:TextView=findViewById(R.id.result)
                    result.text="Not Approved"
                    result.setTextColor(this.getColor(R.color.red))
                }
                else{
                    database=FirebaseDatabase.getInstance()
                    databaseReference1 = database.getReference().child("count")
                    val result:TextView=findViewById(R.id.result)
                    it.ref.child("food").setValue("Yes")
                    result.text="Approved"
                    result.setTextColor(this.getColor(R.color.green))
                    count1=count1+1
                    databaseReference1.setValue(count1)
                }
            }
            else{
                Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

}