package gloomyfish.opencvdemo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.book.chapter.eight.OcrDemoActivity;
import com.book.chapter.five.ImageAnalysisActivity;
import com.book.chapter.four.ConvolutionActivity;
import com.book.chapter.nine.BeautyFaceActivity;
import com.book.chapter.seven.CameraViewActivity;
import com.book.chapter.seven.DisplayModeActivity;
import com.book.chapter.six.Feature2dMainActivity;
import com.book.chapter.ten.EyeRenderActivity;
import com.book.chapter.three.MatOperationsActivity;
import com.book.chapter.two.ReadMatinfoActivity;
import com.book.datamodel.AppConstants;
import com.book.datamodel.ChapterUtils;
import com.book.datamodel.ItemDto;
import com.book.datamodel.SectionsListViewAdaptor;

public class SectionsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sections);
        ItemDto dto = (ItemDto)this.getIntent().getExtras().getSerializable(AppConstants.ITEM_KEY);
        if(dto != null) {
            initListView(dto);
        }
    }

    private void initListView(ItemDto dto) {
        ListView listView = (ListView) findViewById(R.id.secction_listView);
        final SectionsListViewAdaptor commandAdaptor = new SectionsListViewAdaptor(this);
        listView.setAdapter(commandAdaptor);
        commandAdaptor.getDataModel().addAll(ChapterUtils.getSections((int)dto.getId()));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String command = commandAdaptor.getDataModel().get(position).getName();
                goDemoView(command);
            }
        });
        commandAdaptor.notifyDataSetChanged();
    }

    private void goDemoView(String command) {
        if(command.equals(AppConstants.CHAPTER_1TH_PGM_01)) {
            Intent intent = new Intent(this.getApplicationContext(), CharpteFrist1Activity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_2TH_PGM_01)) {
            Intent intent = new Intent(this.getApplicationContext(), ReadMatinfoActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_3TH_PGM)) {
            Intent intent = new Intent(this.getApplicationContext(), MatOperationsActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_4TH_PGM)) {
            Intent intent = new Intent(this.getApplicationContext(), ConvolutionActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_5TH_PGM)) {
            Intent intent = new Intent(this.getApplicationContext(), ImageAnalysisActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_6TH_PGM)) {
            Intent intent = new Intent(this.getApplicationContext(), Feature2dMainActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_7TH_PGM)) {
            Intent intent = new Intent(this.getApplicationContext(), CameraViewActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_7TH_PGM_VIEW_MODE)) {
            Intent intent = new Intent(this.getApplicationContext(), DisplayModeActivity.class);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_8TH_PGM_OCR)) {
            Intent intent = new Intent(this.getApplicationContext(), OcrDemoActivity.class);
            intent.putExtra("TYPE", 1);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_8TH_PGM_ID_NUM)) {
            Intent intent = new Intent(this.getApplicationContext(), OcrDemoActivity.class);
            intent.putExtra("TYPE", 2);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_8TH_PGM_DESKEW)) {
            Intent intent = new Intent(this.getApplicationContext(), OcrDemoActivity.class);
            intent.putExtra("TYPE", 3);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_9TH_PGM_II)) {
            Intent intent = new Intent(this.getApplicationContext(), BeautyFaceActivity.class);
            intent.putExtra("TYPE", 1);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_9TH_PGM_EPF)) {
            Intent intent = new Intent(this.getApplicationContext(), BeautyFaceActivity.class);
            intent.putExtra("TYPE", 2);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_9TH_PGM_FACE)) {
            Intent intent = new Intent(this.getApplicationContext(), BeautyFaceActivity.class);
            intent.putExtra("TYPE", 4);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_9TH_PGM_MASK)) {
            Intent intent = new Intent(this.getApplicationContext(), BeautyFaceActivity.class);
            intent.putExtra("TYPE", 3);
            startActivity(intent);
        }
        else if(command.equals(AppConstants.CHAPTER_10TH_PGM)) {
            Intent intent = new Intent(this.getApplicationContext(), EyeRenderActivity.class);
            intent.putExtra("TYPE", 3);
            startActivity(intent);
        }
    }
}
