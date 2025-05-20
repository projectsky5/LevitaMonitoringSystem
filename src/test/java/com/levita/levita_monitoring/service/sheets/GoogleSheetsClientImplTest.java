package com.levita.levita_monitoring.service.sheets;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleSheetsClientImplTest {

    @Mock Sheets sheetsApi;
    @Mock Sheets.Spreadsheets spreadsheetsApi;
    @Mock Sheets.Spreadsheets.Values valuesApi;
    @Mock Sheets.Spreadsheets.Values.Get getValuesReq;
    @Mock Sheets.Spreadsheets.Values.Append appendReq;
    @Mock Sheets.Spreadsheets.Values.BatchUpdate batchValuesReq;
    @Mock Sheets.Spreadsheets.Get getSpreadsheetReq;
    @Mock Sheets.Spreadsheets.BatchUpdate batchSheetsReq;
    @Mock Sheets.Spreadsheets.Values.BatchGet batchGetReq;

    private final String ssId = "spreadsheet-id";

    @Test
    void getValues_delegatesAndReturns() throws IOException {
        when(sheetsApi.spreadsheets()).thenReturn(spreadsheetsApi);
        when(spreadsheetsApi.values()).thenReturn(valuesApi);
        GoogleSheetsClientImpl client = new GoogleSheetsClientImpl(sheetsApi, ssId);

        String range = "Sheet1!A1:B2";
        ValueRange vr = new ValueRange().setValues(List.of(List.of("x", "y")));
        when(valuesApi.get(ssId, range)).thenReturn(getValuesReq);
        when(getValuesReq.execute()).thenReturn(vr);

        List<List<Object>> out = client.getValues(range);

        assertSame(vr.getValues(), out);
        verify(valuesApi).get(ssId, range);
        verify(getValuesReq).execute();
    }

    @Test
    void appendRow_buildsAndExecutes() throws IOException {
        when(sheetsApi.spreadsheets()).thenReturn(spreadsheetsApi);
        when(spreadsheetsApi.values()).thenReturn(valuesApi);
        GoogleSheetsClientImpl client = new GoogleSheetsClientImpl(sheetsApi, ssId);

        String range = "Sheet1!B2";
        List<Object> row = List.of(1, 2);
        when(valuesApi.append(eq(ssId), eq(range), any(ValueRange.class))).thenReturn(appendReq);
        when(appendReq.setValueInputOption("USER_ENTERED")).thenReturn(appendReq);

        client.appendRow(range, row);

        verify(valuesApi).append(eq(ssId), eq(range), any(ValueRange.class));
        verify(appendReq).setValueInputOption("USER_ENTERED");
        verify(appendReq).execute();
    }

    @Test
    void updateValues_batchesAndExecutes() throws IOException {
        when(sheetsApi.spreadsheets()).thenReturn(spreadsheetsApi);
        when(spreadsheetsApi.values()).thenReturn(valuesApi);
        GoogleSheetsClientImpl client = new GoogleSheetsClientImpl(sheetsApi, ssId);

        String range = "Sheet2!C3";
        List<List<Object>> data = List.of(List.of("a"));
        when(valuesApi.batchUpdate(eq(ssId), any(BatchUpdateValuesRequest.class)))
                .thenReturn(batchValuesReq);

        client.updateValues(range, data);

        ArgumentCaptor<BatchUpdateValuesRequest> cap =
                ArgumentCaptor.forClass(BatchUpdateValuesRequest.class);
        verify(valuesApi).batchUpdate(eq(ssId), cap.capture());
        BatchUpdateValuesRequest req = cap.getValue();
        assertEquals("USER_ENTERED", req.getValueInputOption());
        assertEquals(1, req.getData().size());
        assertEquals(range, req.getData().get(0).getRange());
        assertSame(data, req.getData().get(0).getValues());
        verify(batchValuesReq).execute();
    }

    @Test
    void getSheetIdByName_foundAndNotFound() throws IOException {
        when(sheetsApi.spreadsheets()).thenReturn(spreadsheetsApi);
        GoogleSheetsClientImpl client = new GoogleSheetsClientImpl(sheetsApi, ssId);

        String name = "Target";
        Sheet s1 = new Sheet().setProperties(new SheetProperties().setTitle("X").setSheetId(1));
        Sheet s2 = new Sheet().setProperties(new SheetProperties().setTitle(name).setSheetId(42));
        Spreadsheet ss = new Spreadsheet().setSheets(Arrays.asList(s1, s2));
        when(spreadsheetsApi.get(ssId)).thenReturn(getSpreadsheetReq);
        when(getSpreadsheetReq.execute()).thenReturn(ss);

        int id = client.getSheetIdByName(name);
        assertEquals(42, id);

        when(getSpreadsheetReq.execute()).thenReturn(new Spreadsheet().setSheets(List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> client.getSheetIdByName("Missing"));
    }

    @Test
    void deleteRows_sortsDescendingAndExecutes() throws IOException {
        when(sheetsApi.spreadsheets()).thenReturn(spreadsheetsApi);
        GoogleSheetsClientImpl client = new GoogleSheetsClientImpl(sheetsApi, ssId);

        int sheetId = 5;
        Spreadsheet ss = new Spreadsheet().setSheets(
                List.of(new Sheet().setProperties(new SheetProperties().setTitle("S").setSheetId(sheetId)))
        );
        when(spreadsheetsApi.get(ssId)).thenReturn(getSpreadsheetReq);
        when(getSpreadsheetReq.execute()).thenReturn(ss);
        when(spreadsheetsApi.batchUpdate(eq(ssId), any(BatchUpdateSpreadsheetRequest.class)))
                .thenReturn(batchSheetsReq);

        client.deleteRows("S", new ArrayList<>(List.of(1, 3, 2)));

        ArgumentCaptor<BatchUpdateSpreadsheetRequest> cap =
                ArgumentCaptor.forClass(BatchUpdateSpreadsheetRequest.class);
        verify(spreadsheetsApi).batchUpdate(eq(ssId), cap.capture());
        BatchUpdateSpreadsheetRequest br = cap.getValue();
        assertEquals(3, br.getRequests().size());
        DeleteDimensionRequest dd = br.getRequests().get(0).getDeleteDimension();
        assertEquals(sheetId, dd.getRange().getSheetId());
        assertEquals(2, dd.getRange().getStartIndex());  // 3-1
        assertEquals(3, dd.getRange().getEndIndex());
        verify(batchSheetsReq).execute();
    }

    @Test
    void batchGetValues_setsRangesAndReturns() throws IOException {
        when(sheetsApi.spreadsheets()).thenReturn(spreadsheetsApi);
        when(spreadsheetsApi.values()).thenReturn(valuesApi);
        GoogleSheetsClientImpl client = new GoogleSheetsClientImpl(sheetsApi, ssId);

        List<String> ranges = List.of("A","B");
        BatchGetValuesResponse resp = new BatchGetValuesResponse()
                .setValueRanges(List.of(new ValueRange().setRange("A")));
        when(valuesApi.batchGet(ssId)).thenReturn(batchGetReq);
        when(batchGetReq.setRanges(ranges)).thenReturn(batchGetReq);
        when(batchGetReq.execute()).thenReturn(resp);

        List<ValueRange> out = client.batchGetValues(ssId, ranges);
        assertSame(resp.getValueRanges(), out);
    }
}