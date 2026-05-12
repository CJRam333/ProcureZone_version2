/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



function getAddRow() {

    var row = $('#example1 tr').size() - 2;
    var $str = $('#example1 tr:last').html();
    $('#example1 tr:last').after("<tr>" + $str + "</tr>");

    var tr = $('#example1 tr:last');
    tr.find('input[name=md]').attr('id', 'md' + (row + 1));
    $('#md' + (row + 1)).val("");

    tr.find('input[name=umo]').attr('id', 'umo' + (row + 1));
    $('#umo' + (row + 1)).val("");

    tr.find('input[name=qty]').attr('id', 'qty' + (row + 1));
    $('#md' + (row + 1)).val("");

    tr.find('input[name=stock]').attr('id', 'stock' + (row + 1));
    $('#stock' + (row + 1)).val("");
    this.onchangeevent();

}

function getRemoveRow() {
    try {
        var row = $('#example1 tr').size() - 2;
        if (row > 0) {
            $('#example1 tr:last').remove();
        }
    } catch (e) {
        alert(e);
    }
    this.onchangeevent();
}