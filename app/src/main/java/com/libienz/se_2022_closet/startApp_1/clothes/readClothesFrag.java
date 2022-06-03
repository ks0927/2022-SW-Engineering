package com.libienz.se_2022_closet.startApp_1.clothes;

import static com.libienz.se_2022_closet.startApp_1.util.FirebaseReference.userRef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.libienz.se_2022_closet.R;
import com.libienz.se_2022_closet.startApp_1.data.Clothes;

import java.util.ArrayList;

public class readClothesFrag extends Fragment {

    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference().child("clothes").child(user.getUid());

    //열람할 의류의 키값, 나중에 프래그먼트 간 통신을 통해 [홈 > 열람] 또는 [검색 > 열람]으로 값을 받아올 것
    private String ClothesKey = "1364804085";

    private ArrayList<String> tag;
    private String info;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_read_clothes, container, false);

        //다른 프래그먼트에서 열람할 ClothesKey 받아오는 경우
        if (getArguments() != null) {
            ClothesKey = getArguments().getString("ClothesKey");
        }
        //다른 액티비티에서 열람할 ClothesKey 받아오는 경우
        else {

        }

        //의류 정보를 띄우는 코드
        userRef.child(user.getUid()).child("Clothes").child(ClothesKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Clothes clothes = snapshot.getValue(Clothes.class);

                Log.d("cloth",clothes.toString());
                tag = clothes.getClothesTag();
                info = clothes.getClothesInfo();

                ImageView readimg_iv = (ImageView) view.findViewById(R.id.readClothes_iv);
                TextView readTag_tv = (TextView) view.findViewById(R.id.readTag_tv);
                TextView readInfo_tv = (TextView) view.findViewById(R.id.readInfo_tv);

                //태그 정보 출력
                for(int i = 0; i < tag.size(); i++)
                    readTag_tv.append("#" + tag.get(i) + " ");

                //의류 정보 출력
                readInfo_tv.setText("  " + info);

                //이미지 정보 출력
                //저장된 사진이 없을 경우 실패 메시지 출력, 있을 경우 사진 출력
                if (storageReference == null) {
                    Toast.makeText(container.getContext(), "사진 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                }
                else {
                    StorageReference submitReference = storageReference.child(ClothesKey + ".png");
                    submitReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            if (getActivity() == null) return;
                            Glide.with(readClothesFrag.this).load(uri).into(readimg_iv);
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(container.getContext(), "의류 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //의류 수정 버튼을 클릭했을 때
        Button editClothes_btn = (Button) view.findViewById(R.id.editClothes_btn);
        editClothes_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //열람 중이었던 의류의 키값을 수정 프래그먼트에 넘김
                Bundle bundle = new Bundle();
                bundle.putString("ClothesKey", ClothesKey);

                editClothesFrag editClothesFrag = new editClothesFrag();
                editClothesFrag.setArguments(bundle);

                //열람 중이었던 의류를 수정하도록 함
                getParentFragmentManager().beginTransaction().replace(R.id.readClothes_fg, editClothesFrag).commit();
            }
        });

        //의류 삭제 버튼을 클릭했을 때
        Button deleteClothes_btn = (Button) view.findViewById(R.id.deleteClothes_btn);
        deleteClothes_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                //실시간 데이터베이스에서 데이터 삭제
                userRef.child(user.getUid()).child("Clothes").child(ClothesKey).setValue(null);

                //스토리지에서 사진 삭제
                storageReference.child(ClothesKey + ".png").delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Toast.makeText(container.getContext(), "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                });

                //열람 이전에 보고 있던 화면으로 돌아감
                getParentFragmentManager().beginTransaction().remove(readClothesFrag.this).commit();
            }
        });

        //확인 버튼을 클릭했을 때 열람 이전에 보고 있던 화면으로 돌어감
        Button readtomain_btn = (Button) view.findViewById(R.id.readtomain_btn);
        readtomain_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                getParentFragmentManager().beginTransaction().remove(readClothesFrag.this).commit();
            }
        });

        return view;
    }
    /*
    수정할 태그 값을 입력 -> 이 태그를 어떤 태그로 수정하나요? 새로운 레이아웃에서 받음 -> 기존의 태그를 바꿀 태그로 치환
    */
    private Button editHashTag_btn;
    private EditText editHashTag_et;
    private String prev_tag;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_hashtag);

        editHashTag_btn = (Button) findViewById(R.id.editHashTag_btn);
        editHashTag_et = (EditText) findViewById(R.id.editHashTag_et);
        editHashTag_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //버튼을 누르면 수정할 태그의 이름을 임시 저장
                prev_tag = editHashTag_et.getText().toString();
                for (String str : tag) {
                    if (str == prev_tag) {
                        //해당 태그가 존재할 시 레이아웃 전환
                        Intent intent = new Intent(searchOutfitActivity.this, editHashTagActivity.class);
                        intent.putExtra("prev_tag", prev_tag);
                        startActivity(intent);
                    } else {
                        //팝업창 푸시 '해당 태그가 존재하지 않습니다.'
                    }
                }
            }
        });
    }
}
